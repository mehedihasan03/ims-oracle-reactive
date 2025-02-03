package net.celloscope.mraims.loanportfolio.features.transactionadjustment.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.application.port.in.TransactionAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.application.port.out.TransactionAdjustmentPersistencePort;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.TransactionAdjustment;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.dto.request.TransactionAdjustmentRequestDto;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.dto.response.TransactionAdjustmentResponseDto;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionAdjustmentService implements TransactionAdjustmentUseCase {
    private final TransactionAdjustmentPersistencePort port;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final TransactionUseCase transactionUseCase;
    private final ModelMapper modelMapper;
    private final IStagingDataUseCase stagingDataUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final PassbookUseCase passbookUseCase;
    private final TransactionalOperator transactionalOperator;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final CommonRepository commonRepository;


    public TransactionAdjustmentService(TransactionAdjustmentPersistencePort port, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, TransactionUseCase transactionUseCase, ModelMapper modelMapper, IStagingDataUseCase stagingDataUseCase, OfficeEventTrackerUseCase officeEventTrackerUseCase, PassbookUseCase passbookUseCase, TransactionalOperator transactionalOperator, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, CommonRepository commonRepository) {
        this.port = port;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.transactionUseCase = transactionUseCase;
        this.modelMapper = modelMapper;
        this.stagingDataUseCase = stagingDataUseCase;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.passbookUseCase = passbookUseCase;
        this.transactionalOperator = transactionalOperator;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.commonRepository = commonRepository;
    }

    @Override
    public Mono<TransactionAdjustmentResponseDto> adjustTransaction(TransactionAdjustmentRequestDto requestDto) {
        TransactionAdjustment transactionAdjustment = modelMapper.map(requestDto, TransactionAdjustment.class);
        AtomicReference<List<String>> laterTransactionIds = new AtomicReference<>(new ArrayList<>());
        return this.validateIfTransactionAdjustmentIsPossible(requestDto, transactionAdjustment)
                .flatMap(requestDto1 -> this.getAndSetStagingDataId(transactionAdjustment))
                .doOnError(throwable -> log.error("Error Happened while getting Staging Data Id : {}", throwable.getMessage()))
                .flatMap(this.buildTransactionList(requestDto, transactionAdjustment))
                .doOnError(throwable -> log.error("Error Happened while building Transaction : {}", throwable.getMessage()))
                .flatMap(transactionList -> this.archivePassbookAndUpdateRepaymentSchedule(transactionAdjustment)
                        .map(tuple2 -> {
                            laterTransactionIds.set(tuple2.getT1());
                            return transactionList;
                        }))
                .onErrorResume(throwable -> throwable instanceof IllegalArgumentException
                        ? Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT, throwable.getMessage()))
                        : Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage())))
                .doOnNext(transactionList -> log.info("Transaction List to be saved: {}", transactionList))
                .flatMap(this.saveTransaction())
                .switchIfEmpty(Mono.just(Transaction
                        .builder()
                        .build()))
                .doOnError(throwable -> log.error("Error Happened while saving Adjustment Transaction : {}", throwable.getMessage()))
                .doOnNext(transaction -> this.setAdjustmentTransactionInfo(transaction, transactionAdjustment))
                .map(transaction -> this.calculateAndSetNetAmountAndJournalEntryType(transactionAdjustment))
                .flatMap(this.setProductId())
                .flatMap(port::saveTransactionAdjustment)
                .flatMap(tuple2 -> this.createPassbookEntries(laterTransactionIds.get(), transactionAdjustment))
                .map(savedTransactionAdjustment -> TransactionAdjustmentResponseDto
                        .builder()
                        .userMessage("Transaction Adjustment Successful")
                        .data(savedTransactionAdjustment)
                        .build())
                .as(transactionalOperator::transactional);
    }

    @Override
    public Flux<TransactionAdjustmentResponseDto> getTransactionAdjustmentsByManagementProcessIdAndTransactionCode(String managementProcessId, String transactionCode) {
        return port.getTransactionAdjustmentByManagementProcessIdAndTransactionCode(managementProcessId, transactionCode)
                .map(transactionAdjustment -> TransactionAdjustmentResponseDto
                        .builder()
                        .userMessage("Transaction Adjustment Found")
                        .data(transactionAdjustment)
                        .build());
    }

    @Override
    public Flux<TransactionAdjustmentResponseDto> getTransactionAdjustmentsByManagementProcessIdAndTransactionCodeAndSavingsType(String managementProcessId, String transactionCode, String savingsTypeId) {
        return port.getTransactionAdjustmentByManagementProcessIdAndTransactionCodeAndSavingsType(managementProcessId, transactionCode, savingsTypeId)
                .map(transactionAdjustment -> TransactionAdjustmentResponseDto
                        .builder()
                        .userMessage("Transaction Adjustment Found")
                        .data(transactionAdjustment)
                        .build());
    }

    @Override
    public Flux<TransactionAdjustmentResponseDto> getTransactionAdjustmentsByManagementProcessIdAndTransactionCodeAndPaymentMode(String managementProcessId, String transactionCode, String paymentMode) {
        return port.getTransactionAdjustmentByManagementProcessIdAndTransactionCodeAndPaymentMode(managementProcessId, transactionCode, paymentMode)
                .map(transactionAdjustment -> TransactionAdjustmentResponseDto
                        .builder()
                        .userMessage("Transaction Adjustment Found")
                        .data(transactionAdjustment)
                        .build());
    }

    private Function<TransactionAdjustment, Mono<TransactionAdjustment>> setProductId () {
        return transactionAdjustment -> {
            if (transactionAdjustment.getLoanAccountId() != null) {
                return commonRepository.getLoanAccountEntityByLoanAccountId(transactionAdjustment.getLoanAccountId())
                        .map(entity -> {
                            transactionAdjustment.setLoanProductId(entity.getLoanProductId());
                            return transactionAdjustment;
                        })
                        .doOnError(e -> log.error("Error while setting loan product id: {}", e.getMessage()));
            } else if (transactionAdjustment.getSavingsAccountId() != null) {
                return commonRepository.getSavingsProductDetailsBySavingsAccountList(List.of(transactionAdjustment.getSavingsAccountId()))
                        .reduce((entity1, entity2) -> entity1)
                        .map(entity -> {
                            transactionAdjustment.setSavingsProductId(entity.getProductId());
                            return transactionAdjustment;
                        })
                        .doOnError(e -> log.error("Error while setting savings product id: {}", e.getMessage()));
            }
            return Mono.just(transactionAdjustment);
        };
    }

    private Function<List<Transaction>, Mono<Transaction>> saveTransaction() {
        return transactionList -> Flux.fromIterable(transactionList)
                .flatMap(transaction -> transactionUseCase.getTransactionByTransactionId(transaction.getTransactionId())
                        .switchIfEmpty(Mono.just(Transaction.builder().build()))
                        .flatMap(transaction1 -> transaction1.getTransactionId() == null
                                ? transactionUseCase.saveTransaction(transaction)
                                .doOnRequest(l -> log.info("Transaction to be saved: {}", transaction1))
                                : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Adjustment Transaction already exists"))))
                .filter(transaction -> transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_ADJUSTMENT_LOAN_REPAY.getValue())
                        || transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_ADJUSTMENT_SAVINGS_DEPOSIT.getValue())
                        || transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_ADJUSTMENT_SAVINGS_WITHDRAW.getValue()))
                .reduce((transaction1, transaction2) -> transaction1);
    }

    private Function<TransactionAdjustment, Mono<? extends List<Transaction>>> buildTransactionList(TransactionAdjustmentRequestDto requestDto, TransactionAdjustment transactionAdjustment) {
        return transactionAdjustment1 -> Strings.isNotNullAndNotEmpty(requestDto.getTransactionId())
                ? this.getTransactionByTransactionId(requestDto.getTransactionId())
                    .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Transaction not found")))
                    .doOnNext(transaction -> this.setExistingTransactionInfo(transaction, transactionAdjustment1))
                    .map(transaction -> this.buildTransaction(transaction, transactionAdjustment1))
                    .flatMap(transactionList -> this.setTransactionPrincipalAndServiceCharge(transactionList, requestDto, transactionAdjustment))
                : Mono.just(this.buildTransaction(null, transactionAdjustment1));
    }

    private Mono<List<Transaction>> setTransactionPrincipalAndServiceCharge(List<Transaction> transactionList, TransactionAdjustmentRequestDto requestDto, TransactionAdjustment transactionAdjustment) {
        // ignore process for savings account
        if (Strings.isNullOrEmpty(requestDto.getLoanAccountId())) return Mono.just(transactionList);

        return passbookUseCase.getPassbookEntriesByTransactionId(requestDto.getTransactionId())
                        .collect(
                            this::createInitialPrincipalServiceChargeMap,
                            (containerMap, entry) -> {
                                // Accumulate results in the map
                                containerMap.put("totalPrinPaid", containerMap.get("totalPrinPaid").add(entry.getPrinPaid()));
                                containerMap.put("totalSCPaid", containerMap.get("totalSCPaid").add(entry.getServiceChargePaid()));
                            }
                        )
                        .defaultIfEmpty(createInitialPrincipalServiceChargeMap())
                        .map(resultMap -> {
                            // Map the results to the transaction adjustment
                            transactionAdjustment.setTransactionPrincipal(resultMap.get("totalPrinPaid"));
                            transactionAdjustment.setTransactionServiceCharge(resultMap.get("totalSCPaid"));
                            return transactionList;
                        })
                        .doOnError(e -> log.error("Error while setting transaction principal and service charge: {}", e.getMessage()));
    }

    private Map<String, BigDecimal> createInitialPrincipalServiceChargeMap() {
        Map<String, BigDecimal> initialMap = new HashMap<>();
        initialMap.put("totalPrinPaid", BigDecimal.ZERO);
        initialMap.put("totalSCPaid", BigDecimal.ZERO);
        return initialMap;
    }

    private Mono<TransactionAdjustment> createPassbookEntries(List<String> laterTransactionIds, TransactionAdjustment transactionAdjustment) {
        LinkedList<String> transactionIds = new LinkedList<>();
//        transactionIds.add(transactionAdjustment.getAdjustmentTransactionId());
        if (transactionAdjustment.getAdjustmentTransactionId() != null) {
            transactionIds.add(transactionAdjustment.getAdjustmentTransactionId());
        }
        transactionIds.addAll(laterTransactionIds);
        log.info("transaction ids to create passbook with : {}", transactionIds);
        return Flux.fromIterable(transactionIds)
                .concatMap(transactionUseCase::getTransactionByTransactionId) // Sequential processing starts here
                .filter(transaction -> transaction.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .doOnNext(transaction -> log.info("Creating passbook entry for transactionId: {} and amount: {}", transaction.getTransactionId(), transaction.getAmount()))
                .concatMap(transaction -> {
                    if (transaction.getAccountType().equals(Constants.ACCOUNT_TYPE_LOAN.getValue())) {
                        // Loan transaction handling
                        return handleLoanTransaction(transaction)
                                .doOnSuccess(result -> log.info("Completed passbook entry for loan transactionId: {}", transaction.getTransactionId()))
                                .onErrorResume(e -> {
                                    log.error("Error processing loan transactionId: {}, error: {}", transaction.getTransactionId(), e.getMessage());
                                    return Mono.empty(); // Prevent failure from halting subsequent transactions
                                });
                    } else {
                        // Savings transaction handling
                        return handleSavingsTransaction(transaction, transactionAdjustment)
                                .doOnSuccess(result -> log.info("Completed passbook entry for savings transactionId: {}", transaction.getTransactionId()))
                                .onErrorResume(e -> {
                                    log.error("Error processing savings transactionId: {}, error: {}", transaction.getTransactionId(), e.getMessage());
                                    return Mono.empty(); // Prevent failure from halting subsequent transactions
                                });
                    }
                })
                .collectList() // Waits for all transactions to be processed
                .thenReturn(transactionAdjustment) // Returns the original adjustment after all processing is done
                .doOnError(e -> log.error("Error while creating passbook entries: {}", e.getMessage()));

    }

    private Mono<Boolean> handleLoanTransaction(Transaction transaction) {
        return passbookUseCase.getRepaymentScheduleAndCreatePassbookEntryForLoan(
                        PassbookRequestDTO.builder()
                                .managementProcessId(transaction.getManagementProcessId())
                                .processId(transaction.getProcessId())
                                .officeId(transaction.getOfficeId())
                                .amount(transaction.getAmount())
                                .loanAccountId(transaction.getLoanAccountId())
                                .transactionId(transaction.getTransactionId())
                                .transactionCode(transaction.getTransactionCode())
                                .loginId(transaction.getCreatedBy())
                                .mfiId(transaction.getMfiId())
                                .transactionDate(transaction.getTransactionDate())
                                .paymentMode(transaction.getPaymentMode())
                                .source(transaction.getSource())
                                .build()
                )
                .map(this::getFullyPaidInstallmentNos)
                .flatMapMany(tuple2 -> {
                    if (!tuple2.getT2().isEmpty()) {
                        return loanRepaymentScheduleUseCase.updateInstallmentStatus(
                                tuple2.getT2(), Status.STATUS_PAID.getValue(), tuple2.getT1(), transaction.getManagementProcessId()
                        );
                    }
                    return Flux.just(RepaymentScheduleResponseDTO.builder().build());
                })
                .collectList()
                .map(list -> true);
    }

    private Mono<Boolean> handleSavingsTransaction(Transaction transaction, TransactionAdjustment transactionAdjustment) {
        if (transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_ADJUSTMENT_SAVINGS_WITHDRAW.getValue())
                || transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_SAVINGS_WITHDRAW.getValue())) {
            return createSavingsWithdrawPassbookEntry(transaction, transactionAdjustment)
                    .map(passbookResponseDTO -> true);
        } else if (transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_SAVINGS_DEPOSIT.getValue())
                || transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_ADJUSTMENT_SAVINGS_DEPOSIT.getValue())) {
            return createSavingsDepositPassbookEntry(transaction, transactionAdjustment)
                    .map(passbookResponseDTO -> true);
        }
        return Mono.just(false);
    }



    PassbookRequestDTO createPassbookRequestDto(Transaction transaction, TransactionAdjustment transactionAdjustment) {
        List<String> regularTransactionCodes = List.of(Constants.TRANSACTION_CODE_SAVINGS_WITHDRAW.getValue(), Constants.TRANSACTION_CODE_SAVINGS_DEPOSIT.getValue());
        String managementProcessId = regularTransactionCodes.contains(transaction.getTransactionCode())
                ? transaction.getManagementProcessId()
                : transactionAdjustment.getManagementProcessId();

        return PassbookRequestDTO
                .builder()
                .managementProcessId(managementProcessId)
                .processId(transaction.getProcessId())
                .officeId(transactionAdjustment.getOfficeId())
                .amount(transaction.getAmount())
                .loanAccountId(transaction.getLoanAccountId() != null ? transaction.getLoanAccountId() : null)
                .savingsAccountId(transaction.getSavingsAccountId() != null ? transaction.getSavingsAccountId() : null)
                .transactionId(transaction.getTransactionId())
                .transactionCode(transaction.getTransactionCode())
                .loginId(transaction.getCreatedBy())
                .mfiId(transaction.getMfiId())
                .transactionDate(transaction.getTransactionDate())
                .paymentMode(transaction.getPaymentMode())
                .source(transaction.getSource())
                .samityId(transaction.getSamityId())
                .build();
    }

    private Mono<PassbookResponseDTO> createSavingsWithdrawPassbookEntry(Transaction transaction, TransactionAdjustment transactionAdjustment) {
        return passbookUseCase.createPassbookEntryForSavingsWithdraw(this.createPassbookRequestDto(transaction, transactionAdjustment));
    }

    private Mono<List<PassbookResponseDTO>> createSavingsDepositPassbookEntry(Transaction transaction, TransactionAdjustment transactionAdjustment) {
        return passbookUseCase.createPassbookEntryForSavings(this.createPassbookRequestDto(transaction, transactionAdjustment));
    }

    private Tuple2<String, List<Integer>> getFullyPaidInstallmentNos(List<PassbookResponseDTO> passbookResponseDTOList) {
        AtomicReference<String> loanAccountId = new AtomicReference<>();
        log.debug("passbookResponseDTOList : {}", passbookResponseDTOList);
        List<Integer> fulfilledInstallments = passbookResponseDTOList
                .stream()
                .peek(passbookResponseDTO -> log.debug("before filter passbook response dto : {}", passbookResponseDTO))
                .filter(this::isThisInstallmentFullyPaid)
                .peek(passbookResponseDTO -> log.debug("after filter passbook response dto : {}", passbookResponseDTO))
                .peek(passbookResponseDTO -> loanAccountId.set(passbookResponseDTO.getLoanAccountId()))
                .map(PassbookResponseDTO::getInstallNo)
                .peek(integer -> log.debug("fulfilled installments : {}", integer))
                .toList();
        log.debug("fulfilledInstallments : {}", fulfilledInstallments);
        Tuple2<String, List<Integer>> tuples;
        if (fulfilledInstallments.isEmpty()) {
            log.debug("I was here {}", loanAccountId);
            tuples = Tuples.of("", new ArrayList<>());
        } else tuples = Tuples.of(loanAccountId.get(), fulfilledInstallments);
        log.info("loan account id : {}, Fulfilled installments : {}", tuples, fulfilledInstallments);
        return tuples;
    }

    private boolean isThisInstallmentFullyPaid(PassbookResponseDTO passbookResponseDTO) {
        if (passbookResponseDTO.getScRemainForThisInst() != null && passbookResponseDTO.getPrinRemainForThisInst() != null) {
//            return passbookResponseDTO.getScRemainForThisInst().toString().equals("0.00") && passbookResponseDTO.getPrinRemainForThisInst().toString().equals("0.00");
            return passbookResponseDTO.getScRemainForThisInst().compareTo(BigDecimal.ZERO) == 0 && passbookResponseDTO.getPrinRemainForThisInst().compareTo(BigDecimal.ZERO) == 0;
        } else return false;
    }

    private Mono<Tuple2<List<String>, TransactionAdjustment>> archivePassbookAndUpdateRepaymentSchedule(TransactionAdjustment transactionAdjustment) {

        String accountId = transactionAdjustment.getLoanAccountId() != null
                ? transactionAdjustment.getLoanAccountId()
                : transactionAdjustment.getSavingsAccountId();
        String accountType = transactionAdjustment.getLoanAccountId() != null
                ? Constants.ACCOUNT_TYPE_LOAN.getValue()
                : Constants.ACCOUNT_TYPE_SAVINGS.getValue();

        LocalDate startingTransactionDateToArchive = transactionAdjustment.getTransactionDate() != null
                ? transactionAdjustment.getTransactionDate()
                : transactionAdjustment.getAdjustmentTransactionDate();


        return passbookUseCase.archivePassbookEntriesByTransactionDateAndLater(accountId, accountType, startingTransactionDateToArchive, transactionAdjustment.getLoginId(), transactionAdjustment.getManagementProcessId())
                .map(localDateStringMapAndSavingsTypeIdTuple -> {
                    Map<LocalDate, String> localDateStringMap = localDateStringMapAndSavingsTypeIdTuple.getT1();
                    transactionAdjustment.setSavingsTypeId(localDateStringMapAndSavingsTypeIdTuple.getT2().isEmpty() ? null : localDateStringMapAndSavingsTypeIdTuple.getT2());
                    List<String> laterTransactionIds = localDateStringMap.entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByKey())
                            .map(Map.Entry::getValue)
                            .collect(Collectors.toList());
                    log.info("later transaction ids : {}", laterTransactionIds);
                    return laterTransactionIds;
                })
                .map(laterTransactionIds -> Tuples.of(laterTransactionIds, transactionAdjustment))
                .doOnError(throwable -> log.error("Error while archiving passbook entries: {}", throwable.getMessage()));
    }

    private void setExistingTransactionInfo(Transaction transaction, TransactionAdjustment transactionAdjustment) {
        transactionAdjustment.setTransactionId(transaction.getTransactionId());
        transactionAdjustment.setTransactionAmount(transaction.getAmount());
        transactionAdjustment.setTransactionDate(transaction.getTransactionDate());
    }

    private void setAdjustmentTransactionInfo(Transaction transaction, TransactionAdjustment transactionAdjustment) {
        transactionAdjustment.setAdjustmentTransactionId(transaction.getTransactionId());
        transactionAdjustment.setAdjustmentTransactionAmount(transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO);
        transactionAdjustment.setAdjustmentTransactionDate(transaction.getTransactionDate());

    }

    private TransactionAdjustment calculateAndSetNetAmountAndJournalEntryType(TransactionAdjustment transactionAdjustment) {
        BigDecimal transactionAmount = transactionAdjustment.getTransactionAmount() != null ? transactionAdjustment.getTransactionAmount() : BigDecimal.ZERO;
        BigDecimal netAmount = transactionAdjustment.getAdjustmentTransactionAmount().subtract(transactionAmount);
        transactionAdjustment.setNetAmount(netAmount);
        transactionAdjustment.setCreatedBy(transactionAdjustment.getLoginId());
        transactionAdjustment.setCreatedOn(LocalDateTime.now());
        transactionAdjustment.setStatus(Status.STATUS_PENDING.getValue());

        transactionAdjustment.setJournalEntryType(
                netAmount.compareTo(BigDecimal.ZERO) > 0
                    ? Constants.JOURNAL_ENTRY_TYPE_IN.getValue()
                    : Constants.JOURNAL_ENTRY_TYPE_OUT.getValue());
        return transactionAdjustment;
    }

    private Mono<TransactionAdjustment> getAndSetStagingDataId(TransactionAdjustment transactionAdjustment) {
        return stagingDataUseCase.getStagingDataByMemberId(transactionAdjustment.getMemberId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data not found for member")))
                .doOnNext(stagingData -> transactionAdjustment.setStagingDataId(stagingData.getStagingDataId()))
                .thenReturn(transactionAdjustment)
                .doOnError(throwable -> log.error("Error Happened while getting Staging Data Id : {}", throwable.getMessage()));
    }

    private List<Transaction> buildTransaction(Transaction transaction, TransactionAdjustment transactionAdjustment) {
        log.info("Transaction to be adjusted: {}", transaction);

        LinkedList<Transaction> transactions = new LinkedList<>();
        String processId = UUID.randomUUID().toString();

        if (transaction != null) {
            String reverseTransactionCode = null;
            if (transaction.getLoanAccountId() != null) {
                if (transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_LOAN_REPAY.getValue())
                        || transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_ADJUSTMENT_LOAN_REPAY.getValue())) {
                    reverseTransactionCode = Constants.TRANSACTION_CODE_REVERSE_LOAN_REPAY.getValue();
                } else throw new IllegalArgumentException("Invalid Loan Transaction Code");
            } else if (transaction.getSavingsAccountId() != null) {
                if (transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_SAVINGS_DEPOSIT.getValue())
                        || transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_ADJUSTMENT_SAVINGS_DEPOSIT.getValue())) {
                    reverseTransactionCode = Constants.TRANSACTION_CODE_REVERSE_SAVINGS_DEPOSIT.getValue();
                } else if (transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_SAVINGS_WITHDRAW.getValue())
                        || transaction.getTransactionCode().equals(Constants.TRANSACTION_CODE_ADJUSTMENT_SAVINGS_WITHDRAW.getValue())) {
                    reverseTransactionCode = Constants.TRANSACTION_CODE_REVERSE_SAVINGS_WITHDRAW.getValue();
                } else throw new IllegalArgumentException("Invalid Savings Transaction Code");
            }


            Transaction reverseTransaction =
                    Transaction
                            .builder()
                            .transactionId("R-".concat(transaction.getTransactionId()))
                            .managementProcessId(transactionAdjustment.getManagementProcessId())
                            .processId(processId)
                            .memberId(transaction.getMemberId())
                            .stagingDataId(transactionAdjustment.getStagingDataId())
                            .loanAccountId(transaction.getLoanAccountId())
                            .savingsAccountId(transaction.getSavingsAccountId())
                            .amount(transaction.getAmount())
                            .transactionCode(reverseTransactionCode)
                            .accountType(transactionAdjustment.getLoanAccountId() != null
                                    ? Constants.ACCOUNT_TYPE_LOAN.getValue()
                                    : Constants.ACCOUNT_TYPE_SAVINGS.getValue())
                            .paymentMode(transaction.getPaymentMode())
                            .mfiId(transaction.getMfiId())
                            .transactionDate(transactionAdjustment.getBusinessDate())
                            .transactedBy(transactionAdjustment.getLoginId())
                            .createdOn(LocalDateTime.now())
                            .createdBy(transactionAdjustment.getLoginId())
                            .status(Status.STATUS_APPROVED.getValue())
                            .officeId(transaction.getOfficeId())
                            .build();
            log.info("Reverse Transaction Built: {}", reverseTransaction);
            transactions.add(reverseTransaction);

            transactionAdjustment.setReverseTransactionId(reverseTransaction.getTransactionId());
            transactionAdjustment.setReverseTransactionDate(reverseTransaction.getTransactionDate());
            transactionAdjustment.setReverseTransactionAmount(reverseTransaction.getAmount());
            transactionAdjustment.setTransactionCode(reverseTransactionCode);
        }

        // If the adjustment amount is zero, no need to create a correction transaction
        if (transactionAdjustment.getAdjustmentTransactionAmount().compareTo(BigDecimal.ZERO) == 0) {
            return transactions;
        }

        String adjustmentTransactionCode = null;

        if (transactionAdjustment.getLoanAccountId() != null) {
            adjustmentTransactionCode = Constants.TRANSACTION_CODE_ADJUSTMENT_LOAN_REPAY.getValue();
        } else if (transactionAdjustment.getSavingsAccountId() != null && transactionAdjustment.getSavingsTransactionType().equals(Constants.SAVINGS_TRANSACTION_ADJUSTMENT_TYPE_DEPOSIT.getValue())) {
            adjustmentTransactionCode = Constants.TRANSACTION_CODE_ADJUSTMENT_SAVINGS_DEPOSIT.getValue();
        } else if (transactionAdjustment.getSavingsAccountId() != null && transactionAdjustment.getSavingsTransactionType().equals(Constants.SAVINGS_TRANSACTION_ADJUSTMENT_TYPE_WITHDRAW.getValue())) {
            adjustmentTransactionCode = Constants.TRANSACTION_CODE_ADJUSTMENT_SAVINGS_WITHDRAW.getValue();
        } else {
            throw new IllegalArgumentException("Invalid Adjustment Transaction Payload! Check loanAccountId, savingsAccountId & savingsTransactionType");
        }

        Transaction adjustmentTransaction =
                Transaction
                    .builder()
                        .transactionId(UUID.randomUUID().toString())
                        .managementProcessId(transactionAdjustment.getManagementProcessId())
                        .processId(processId)
                        .memberId(transactionAdjustment.getMemberId())
                        .stagingDataId(transactionAdjustment.getStagingDataId())
                        .loanAccountId(transactionAdjustment.getLoanAccountId())
                        .savingsAccountId(transactionAdjustment.getSavingsAccountId())
                        .amount(transactionAdjustment.getAdjustmentTransactionAmount())
                        .transactionCode(adjustmentTransactionCode)
                        .accountType(transactionAdjustment.getLoanAccountId() != null
                                ? Constants.ACCOUNT_TYPE_LOAN.getValue()
                                : Constants.ACCOUNT_TYPE_SAVINGS.getValue())
                        .paymentMode(transactionAdjustment.getPaymentMode())
//                        .collectionType(collectionType)
                        .mfiId(transactionAdjustment.getMfiId())
                        .transactionDate(transactionAdjustment.getAdjustmentTransactionDate())
                        .transactedBy(transactionAdjustment.getLoginId())
                        .createdOn(LocalDateTime.now())
                        .createdBy(transactionAdjustment.getLoginId())
                        .status(Status.STATUS_APPROVED.getValue())
                        .officeId(transactionAdjustment.getOfficeId())
                    .build();

        log.info("Adjustment Transaction Built: {}", adjustmentTransaction);
        transactions.add(adjustmentTransaction);
        return transactions;
    }

    private Mono<Transaction> getTransactionByTransactionId(String transactionId) {
        return transactionUseCase.getTransactionByTransactionId(transactionId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Transaction not found")))
                .doOnError(throwable -> log.error("Error Happened while getting Transaction by Transaction Id : {}", throwable.getMessage()));
    }

    private Mono<TransactionAdjustmentRequestDto> validateIfTransactionAdjustmentIsPossible(TransactionAdjustmentRequestDto requestDto, TransactionAdjustment transactionAdjustment) {
        List<String> validSavingsTransactionAdjustmentTypes = List.of(Constants.SAVINGS_TRANSACTION_ADJUSTMENT_TYPE_DEPOSIT.getValue(), Constants.SAVINGS_TRANSACTION_ADJUSTMENT_TYPE_WITHDRAW.getValue());

        if (Strings.isNotNullAndNotEmpty(requestDto.getSavingsAccountId()) && Strings.isNotNullAndNotEmpty(requestDto.getLoanAccountId())) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Both Loan and Savings Account Ids cannot be present"));
        } else if (requestDto.getLoanAccountId() != null && requestDto.getSavingsTransactionType() != null) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Transaction Type cannot be present for Loan Account"));
        } else if (requestDto.getSavingsAccountId() != null && requestDto.getSavingsTransactionType() == null) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Transaction Type cannot be null for Savings Account"));
        } else if (requestDto.getSavingsAccountId() != null && !validSavingsTransactionAdjustmentTypes.contains(requestDto.getSavingsTransactionType())) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Invalid Savings Transaction Type"));
        }

        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                .switchIfEmpty(Mono.error(new RuntimeException("No management process found for office")))
                .doOnNext(managementProcessTracker -> {
                    transactionAdjustment.setManagementProcessId(managementProcessTracker.getManagementProcessId());
                    transactionAdjustment.setBusinessDate(managementProcessTracker.getBusinessDate());
                })
                .flatMap(managementProcessTracker ->
                        officeEventTrackerUseCase.getAllOfficeEventsForManagementProcessId(managementProcessTracker.getManagementProcessId())
                                .filter(officeEvents -> !officeEvents.isEmpty())
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No office events found for management process")))
                                .flatMap(officeEvents -> validateOfficeEvents(officeEvents, requestDto))
                )
                .thenReturn(requestDto)
                .doOnError(throwable -> log.error("Error Happened while validating Transaction Adjustment Request : {}", throwable.getMessage()));
    }

    private Mono<TransactionAdjustmentRequestDto> validateOfficeEvents(List<OfficeEventTracker> officeEvents, TransactionAdjustmentRequestDto requestDto) {
        if (officeEvents.stream().noneMatch(event -> event.getOfficeEvent().equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging data generation not completed for office!"));
        } else if (officeEvents.stream().anyMatch(event -> event.getOfficeEvent().equals(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue()))) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Auto Voucher already Generated for Office!"));
        } else if (officeEvents.stream().anyMatch(event -> event.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process already completed for Office!"));
        }
        return Mono.just(requestDto);
    }
}
