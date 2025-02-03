package net.celloscope.mraims.loanportfolio.features.dps.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.DPSClosureUseCase;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto.*;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.out.DPSClosurePersistencePort;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPS;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosureDetailView;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosureGridView;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosure;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.FDRClosureDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.AccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;

@Service
@Slf4j
public class DPSClosureService implements DPSClosureUseCase {

    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final DPSClosurePersistencePort port;
    private final PassbookUseCase passbookUseCase;
    private final TransactionUseCase transactionUseCase;
    private final ModelMapper mapper;
    private final TransactionalOperator rxtx;

    public DPSClosureService(ISavingsAccountUseCase savingsAccountUseCase, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, DPSClosurePersistencePort port, PassbookUseCase passbookUseCase, TransactionUseCase transactionUseCase, ModelMapper mapper, TransactionalOperator rxtx) {
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.port = port;
        this.passbookUseCase = passbookUseCase;
        this.transactionUseCase = transactionUseCase;
        this.mapper = mapper;
        this.rxtx = rxtx;
    }

    @Override
    public Mono<DPSGridViewDTO> getDPSGridViewByOffice(DPSGridViewCommand command) {
        return savingsAccountUseCase
//                .getDPSSavingsAccountsByOfficeId(command.getOfficeId())
                .getDPSSavingsAccountsByOfficeIdAndSearchText(command.getOfficeId(), command.getSearchText())
                .sort(Comparator.comparing(DPSAccountDTO::getAcctStartDate).reversed())
                .map(this::buildDPSForGridView)
                .collectList()
                .flatMap(fdrList -> Flux.fromIterable(fdrList)
                        .skip((long) command.getOffset() * command.getLimit())
                        .take(command.getLimit())
                        .collectList()
                        .zipWith(managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId()))
                        .map(tuple -> buildDPSGridViewDTO(tuple.getT1(), tuple.getT2(), fdrList.size()))
                );
    }


    private DPS buildDPSForGridView(DPSAccountDTO dpsAccountDTO) {
        return  DPS
                .builder()
                        .savingsAccountId(dpsAccountDTO.getSavingsAccountId())
                        .memberId(dpsAccountDTO.getMemberId())
                        .memberNameEn(dpsAccountDTO.getMemberNameEn())
                        .memberNameBn(dpsAccountDTO.getMemberNameBn())
                        .acctStartDate(dpsAccountDTO.getAcctStartDate())
                        .savingsAmount(dpsAccountDTO.getSavingsAmount())
                        .acctEndDate(dpsAccountDTO.getAcctEndDate())
                        .balance(dpsAccountDTO.getBalance())
                        .maturityAmount(dpsAccountDTO.getMaturityAmount())
                        .status(dpsAccountDTO.getStatus())
                .build();
    }

    private DPSGridViewDTO buildDPSGridViewDTO(List<DPS> dpsList, ManagementProcessTracker managementProcessTracker, Integer totalDPSAccountsNo) {
        return DPSGridViewDTO
                .builder()
                .officeId(managementProcessTracker.getOfficeId())
                .officeNameEn(managementProcessTracker.getOfficeNameEn())
                .officeNameBn(managementProcessTracker.getOfficeNameBn())
                .businessDate(managementProcessTracker.getBusinessDate())
                .businessDay(managementProcessTracker.getBusinessDay())
                .userMessage("DPS accounts fetched successfully")
                .data(dpsList)
                .totalCount(totalDPSAccountsNo)
                .build();
    }


    @Override
    public Mono<DPSDetailViewDTO> getDPSDetailViewBySavingsAccountId(String savingsAccountId) {
        return savingsAccountUseCase
                .getDPSAccountDetailsBySavingsAccountId(savingsAccountId)
                .map(this::buildDPSForDetailView)
                .map(this::buildDPSDetailViewDTO)
                .flatMap(dpsDetailViewDTO -> port
                        .checkIfDPSClosureExistsBySavingsAccountId(dpsDetailViewDTO.getData().getSavingsAccountId())
                        .flatMap(aBoolean -> {
                            Mono<DPSDetailViewDTO> dpsDetailViewDTOMono;
                            if (aBoolean) {
                                dpsDetailViewDTOMono = port.getDPSClosureBySavingsAccountId(savingsAccountId)
                                    .map(dpsClosure -> {
                                        dpsDetailViewDTO.getData().setAcctCloseDate(dpsClosure.getAcctCloseDate());
                                        dpsDetailViewDTO.getData().setClosingAmount(dpsClosure.getClosingAmount());
                                        dpsDetailViewDTO.getData().setTotalInterest(dpsClosure.getTotalInterest());
                                        dpsDetailViewDTO.setBtnEncashEnabled("No");
                                        return dpsDetailViewDTO;
                                    });
                            } else {
                                dpsDetailViewDTO.setBtnEncashEnabled("Yes");
                                dpsDetailViewDTOMono = Mono.just(dpsDetailViewDTO);
                            }
                            return dpsDetailViewDTOMono;
                        }));
    }

    @Override
    public Mono<DPSClosureDTO> closeDPSAccount(DPSClosureCommand command) {
        return savingsAccountUseCase
                .getDPSAccountDetailsBySavingsAccountId(command.getSavingsAccountId())
                .flatMap(dpsAccountDTO -> Mono.just(dpsAccountDTO.getStatus())
                        .filter(s -> !s.equalsIgnoreCase(Status.STATUS_CLOSED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Account already closed!.")))
                        .then(port
                                .getDPSClosureBySavingsAccountId(command.getSavingsAccountId())
                                .switchIfEmpty(Mono.just(DPSClosure.builder().build()))
                                .flatMap(dpsClosure ->
                                        dpsClosure.getSavingsAccountId() == null || dpsClosure.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())
                                                ? Mono.just(true)
                                                : Mono.just(false))
                                .flatMap(aBoolean -> aBoolean
                                        ? passbookUseCase
                                        .getLastPassbookEntryBySavingsAccount(dpsAccountDTO.getSavingsAccountId())
                                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Passbook Entry found for : "+ dpsAccountDTO.getSavingsAccountId())))
                                        .flatMap(passbookResponseDTO -> command.getMaturityAmount() != null && command.getMaturityAmount().compareTo(BigDecimal.ZERO) > 0
                                                ? this.getTotalInterestAndMaturityAmountForDPSClosureWhenMaturityAmountProvided(passbookResponseDTO, dpsAccountDTO, command)
                                                : this.getTotalInterestAndMaturityAmountForDPSClosureV2(passbookResponseDTO, dpsAccountDTO, command))
                                        : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Closure Request already exists by SavingsAccountId : " + command.getSavingsAccountId())))
                                .flatMap(dpsClosure -> buildDPSClosure(dpsClosure, dpsAccountDTO, command))))
                .flatMap(port::saveDPSClosure)
                .map(dpsClosure -> DPSClosureDTO
                        .builder()
                        .userMessage("DPS Closure build successful")
                        .data(dpsClosure)
                        .build());
    }

    @Override
    public Mono<DPSClosureDTO> authorizeDPSClosure(DPSAuthorizeCommand command) {

        return validateDPSAccount(command.getSavingsAccountId())
                .flatMap(valid -> valid
                        ? port.getDPSClosureBySavingsAccountId(command.getSavingsAccountId())
                        .filter(fdrClosure -> fdrClosure.getStatus().equalsIgnoreCase(Status.STATUS_PENDING_APPROVAL.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Closure Application Cannot be Authorized : " + command.getSavingsAccountId())))
                        .flatMap(dpsClosure -> buildTransactionAndCreatePassbookEntries(dpsClosure, command))
                        .flatMap(passbookResponseDTO -> updateDPSClosureAndSavingsAccount(command.getSavingsAccountId(), command.getLoginId()))
                        .map(dpsClosure -> DPSClosureDTO.builder()
                                .userMessage("DPS Closure Authorization Successful.")
                                .data(dpsClosure)
                                .build())
                        : Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "DPS Closure Application not found for : " + command.getSavingsAccountId())))
                .as(this.rxtx::transactional);
    }


    private Mono<PassbookResponseDTO> buildTransactionAndCreatePassbookEntries(DPSClosure dpsClosure, DPSAuthorizeCommand command) {
        return buildTransactionForDPSClosure(dpsClosure, command)
                .flatMap(transactionUseCase::createTransactionForDPSClosure)
                .map(singleTransactionResponseDTO -> Tuples.of(singleTransactionResponseDTO, dpsClosure.getClosingInterest()))
                .flatMap(transactionInterestTuple -> passbookUseCase.createPassbookEntryForInterestDeposit(
                                buildPassbookRequestDTOForInterestPosting(transactionInterestTuple.getT1(), transactionInterestTuple.getT2(), command))
                        .flatMap(accruedInterestResponseDTO -> passbookUseCase.createPassbookEntryForTermDepositClosure(
                                buildPassbookRequestDTOForDPSClosure(transactionInterestTuple.getT1(), command, accruedInterestResponseDTO))));
    }

    private Mono<DPSClosure> updateDPSClosureAndSavingsAccount(String savingsAccountId, String loginId) {
        return port.updateDPSClosureStatus(savingsAccountId, Status.STATUS_APPROVED.getValue(), loginId)
                .flatMap(dpsClosure ->  savingsAccountUseCase.updateSavingsAccountStatus(savingsAccountId, Status.STATUS_CLOSED.getValue(), loginId)
                        .doOnRequest(l -> log.info("Requesting to update DPS account status to closed for savings account id : {}", savingsAccountId))
                        .doOnSuccess(savingsAccountResponseDTO -> log.info("DPS account status updated to closed successfully for savings account id : {}", savingsAccountId))
                        .thenReturn(dpsClosure));
    }

    @Override
    public Mono<DPSClosureDTO> rejectDPSClosure(DPSAuthorizeCommand command) {
        return port
                .getDPSClosureBySavingsAccountId(command.getSavingsAccountId())
                .doOnRequest(l -> log.info("requesting to reject DPS closure application for savings account id : {}", command.getSavingsAccountId()))
                .filter(fdrClosure -> fdrClosure.getStatus().equalsIgnoreCase(Status.STATUS_PENDING_APPROVAL.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Closure Application Cannot be Rejected : " + command.getSavingsAccountId())))
                .map(dpsClosure -> {
                    dpsClosure.setStatus(Status.STATUS_REJECTED.getValue());
                    dpsClosure.setRejectedBy(command.getLoginId());
                    dpsClosure.setRejectedOn(LocalDateTime.now());
                    dpsClosure.setRemarks(command.getRemarks());
                    return dpsClosure;
                })
                .flatMap(port::saveDPSClosure)
                .map(dpsClosure -> DPSClosureDTO
                        .builder()
                        .userMessage("DPS Closure Rejected Successfully.")
                        .data(dpsClosure)
                        .build());
    }

    @Override
    public Mono<DPSClosureGridViewResponse> getDPSClosureGridViewByOffice(DPSGridViewCommand command) {
        AtomicReference<Integer> totalCount = new AtomicReference<>(0);
        return port
                .getAllDPSClosureByOfficeId(command.getOfficeId())
                .doOnRequest(l -> log.info("requesting to fetch all dps closures by office : {}", command.getOfficeId()))
                .filter(dpsClosure -> dpsClosure.getStatus().equalsIgnoreCase(Status.STATUS_PENDING_APPROVAL.getValue()))
                .filter(dpsClosure -> Strings.isNullOrEmpty(command.getSearchText())
                        || dpsClosure.getMemberId().equalsIgnoreCase(command.getSearchText())
                        || dpsClosure.getSavingsAccountId().equalsIgnoreCase(command.getSearchText()))
                .map(dpsClosure -> mapper.map(dpsClosure, DPSClosureGridView.class))
                .collectList()
                .flatMapMany(dpsClosureList -> {
                    totalCount.set(dpsClosureList.size());
                    return Flux.fromIterable(dpsClosureList)
                            .sort(Comparator.comparing(DPSClosureGridView::getAcctCloseDate).reversed())
                            .skip((long) command.getOffset() * command.getLimit())
                            .take(command.getLimit());
                })
                .collectList()
                .doOnSuccess(dpsClosureList -> log.info("successfully fetched dps closure list by office : {}. DPS Closure List : {}", command.getOfficeId(), dpsClosureList))
                .map(dpsClosureList -> DPSClosureGridViewResponse
                        .builder()
                        .userMessage("DPS Closure Pending List Fetched Successfully.")
                        .data(dpsClosureList)
                        .totalCount(totalCount.get())
                        .build());
    }

    @Override
    public Mono<DPSClosureDetailViewResponse> getDPSClosureDetailViewBySavingsAccountId(String savingsAccountId) {
        return savingsAccountUseCase
                .getDPSAccountDetailsBySavingsAccountId(savingsAccountId)
                .zipWith(port.getDPSClosureBySavingsAccountId(savingsAccountId))
                .map(this::buildDPSAuthorizationDetailView)
                .map(dpsClosureDetailView ->
                        DPSClosureDetailViewResponse
                        .builder()
                                .userMessage("DPS Closure Detail Fetched Successfully.")
                                .data(dpsClosureDetailView)
                                .build());
    }

    @Override
    public Mono<DPSClosureDTO> getDPSClosingInfoBySavingsAccountId(DPSClosureCommand command) {
        return savingsAccountUseCase
                .getDPSAccountDetailsBySavingsAccountId(command.getSavingsAccountId())
                .flatMap(dpsAccountDTO -> Mono.just(dpsAccountDTO.getStatus())
                        .filter(s -> !s.equalsIgnoreCase(Status.STATUS_CLOSED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Account already closed!.")))
                        .then(port
                                .getDPSClosureBySavingsAccountId(command.getSavingsAccountId())
                                .switchIfEmpty(Mono.just(DPSClosure.builder().build()))
                                .flatMap(dpsClosure ->
                                        dpsClosure.getSavingsAccountId() == null || dpsClosure.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())
                                                ? Mono.just(true)
                                                : Mono.just(false))
                                .flatMap(aBoolean -> aBoolean
                                        ? passbookUseCase
                                        .getLastPassbookEntryBySavingsAccount(dpsAccountDTO.getSavingsAccountId())
                                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Passbook Entry found for : "+ dpsAccountDTO.getSavingsAccountId())))
                                        .flatMap(passbookResponseDTO -> this.getTotalInterestAndMaturityAmountForDPSClosureInfo(passbookResponseDTO, dpsAccountDTO, command))
                                        : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Closure Request already exists by SavingsAccountId : " + command.getSavingsAccountId())))
                                .flatMap(dpsClosure -> buildDPSClosure(dpsClosure, dpsAccountDTO, command))))
//                .flatMap(port::saveDPSClosure)
                .map(dpsClosure -> DPSClosureDTO
                        .builder()
                        .userMessage("DPS Closure Info Fetch successful.")
                        .data(dpsClosure)
                        .build());
    }



    private DPSClosureDetailView buildDPSAuthorizationDetailView(Tuple2<DPSAccountDTO, DPSClosure> dpsAccountDpsClosureTuple) {
        DPSAccountDTO dpsAccountDTO = dpsAccountDpsClosureTuple.getT1();
        DPSClosure dpsClosure = dpsAccountDpsClosureTuple.getT2();

        return DPSClosureDetailView
                .builder()
                .savingsAccountId(dpsClosure.getSavingsAccountId())
                .savingsApplicationId(dpsClosure.getSavingsApplicationId())
                .savingsProductId(dpsClosure.getSavingsProductId())
                .savingsProdNameEn(dpsClosure.getSavingsProdNameEn())
                .memberId(dpsClosure.getMemberId())
                .memberNameEn(dpsClosure.getMemberNameEn())
                .memberNameBn(dpsClosure.getMemberNameBn())
                .savingsAmount(dpsClosure.getSavingsAmount())
                .interestRateFrequency(dpsClosure.getInterestRateFrequency())
                .interestRateTerms("Fixed")
                .interestPostingPeriod(dpsClosure.getInterestPostingPeriod())
                .interestCompoundingPeriod(dpsClosure.getInterestCompoundingPeriod())
                .acctStartDate(dpsClosure.getAcctStartDate())
                .acctEndDate(dpsClosure.getAcctEndDate())
                .acctCloseDate(dpsClosure.getAcctCloseDate())
                .closingAmount(dpsClosure.getClosingAmount())
                .totalInterest(dpsClosure.getTotalInterest())
                .effectiveInterestRate(dpsClosure.getEffectiveInterestRate())
                .status(dpsClosure.getStatus())
                .interestRate(dpsAccountDTO.getInterestRate())
                .maturityAmount(dpsAccountDTO.getMaturityAmount())
                .build();
    }


    private PassbookRequestDTO buildPassbookRequestDTOForInterestPosting(SingleTransactionResponseDTO transactionResponseDTO, BigDecimal totalInterest, DPSAuthorizeCommand command) {
        PassbookRequestDTO requestDTO = PassbookRequestDTO
                .builder()
                .amount(totalInterest)
                .managementProcessId(transactionResponseDTO.getManagementProcessId() != null
                        ? transactionResponseDTO.getManagementProcessId()
                        : UUID.randomUUID().toString())
                .processId(transactionResponseDTO.getProcessId() != null
                        ? transactionResponseDTO.getProcessId()
                        : UUID.randomUUID().toString())
                .savingsAccountId(transactionResponseDTO.getSavingsAccountId())
                .transactionId(transactionResponseDTO.getTransactionId())
                .transactionCode(TRANSACTION_CODE_INTEREST_DEPOSIT.getValue())
                .mfiId(transactionResponseDTO.getMfiId())
                .loginId(command.getLoginId())
                .officeId(command.getOfficeId())
                .transactionDate(transactionResponseDTO.getTransactionDate())
                .paymentMode(transactionResponseDTO.getPaymentMode())
                .memberId(transactionResponseDTO.getMemberId())
                .build();

        log.info("Passbook Request DTO For Interest posting : {}", requestDTO);
        return requestDTO;
    }

    Mono<Boolean> validateDPSAccount(String savingsAccountId) {
        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                .map(SavingsAccountResponseDTO::getStatus)
                .filter(s -> !s.equalsIgnoreCase(Status.STATUS_CLOSED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "DPS Account already closed!.")))
                .then(port
                        .checkIfDPSClosureExistsBySavingsAccountId(savingsAccountId));
    }



    PassbookRequestDTO buildPassbookRequestDTOForDPSClosure(SingleTransactionResponseDTO transaction, DPSAuthorizeCommand command, AccruedInterestResponseDTO accruedInterestResponseDTO) {
        return PassbookRequestDTO
                .builder()
                .managementProcessId(transaction.getManagementProcessId())
                .processId(UUID.randomUUID().toString())
                .amount(transaction.getAmount())
                .savingsAccountId(transaction.getSavingsAccountId())
                .transactionId(transaction.getTransactionId())
                .transactionCode(transaction.getTransactionCode())
                .loginId(command.getLoginId())
                .mfiId(transaction.getMfiId())
                .officeId(command.getOfficeId())
                .transactionDate(transaction.getTransactionDate())
                .paymentMode(transaction.getPaymentMode())
                .savgAcctBeginBalance(accruedInterestResponseDTO.getSavgAcctEndingBalance())
                .totalDepositAmount(accruedInterestResponseDTO.getTotalDepositAmount())
                .totalAccruedInterDeposit(accruedInterestResponseDTO.getTotalAccruedInterDeposit())
                .build();
    }


    Mono<Transaction> buildTransactionForDPSClosure(DPSClosure dpsClosure, DPSAuthorizeCommand command) {
        return managementProcessTrackerUseCase.getLastManagementProcessIdForOffice(command.getOfficeId())
                .map(managementProcessId -> Transaction
                        .builder()
                        .managementProcessId(managementProcessId)
                        .processId(UUID.randomUUID().toString())
                        .accountType(ACCOUNT_TYPE_SAVINGS.getValue())
                        .savingsAccountId(dpsClosure.getSavingsAccountId())
                        .transactionId(UUID.randomUUID().toString())
                        .memberId(dpsClosure.getMemberId())
                        .amount(dpsClosure.getClosingAmount())
//                        .transactionCode(TRANSACTION_CODE_DPS_CLOSURE.getValue())
                        .transactionCode(TRANSACTION_CODE_SAVINGS_WITHDRAW.getValue())
                        .mfiId(command.getMfiId())
                        .officeId(command.getOfficeId())
                        .transactionDate(dpsClosure.getAcctCloseDate())
                        .transactedBy(command.getLoginId())
                        .createdOn(LocalDateTime.now())
                        .createdBy(command.getLoginId())
                        .paymentMode(dpsClosure.getPaymentMode())
                        .status(Status.STATUS_APPROVED.getValue())
                        .build());
    }


    Mono<DPSClosure> buildDPSClosure(DPSClosure dpsClosure, DPSAccountDTO dpsAccountDTO, DPSClosureCommand command) {
        return managementProcessTrackerUseCase.getLastManagementProcessIdForOffice(command.getOfficeId())
                        .map(managementProcessId -> {
                            dpsClosure.setSavingsAccountId(command.getSavingsAccountId());
                            dpsClosure.setAcctStartDate(dpsAccountDTO.getAcctStartDate());
                            dpsClosure.setAcctEndDate(dpsAccountDTO.getAcctEndDate());
                            dpsClosure.setAcctCloseDate(command.getClosingDate());
                            dpsClosure.setPaymentMode(command.getPaymentMode());
                            dpsClosure.setSavingsAmount(dpsAccountDTO.getSavingsAmount());
                            dpsClosure.setReferenceAccountId(command.getReferenceAccountId());

                            dpsClosure.setCreatedBy(command.getLoginId());
                            dpsClosure.setCreatedOn(LocalDateTime.now());
                            dpsClosure.setMemberId(dpsAccountDTO.getMemberId());
                            dpsClosure.setMemberNameEn(dpsAccountDTO.getMemberNameEn());
                            dpsClosure.setMemberNameBn(dpsAccountDTO.getMemberNameBn());

                            dpsClosure.setSavingsApplicationId(dpsAccountDTO.getSavingsApplicationId());
                            dpsClosure.setSavingsProductId(dpsAccountDTO.getSavingsProductId());
                            dpsClosure.setSavingsProdNameEn(dpsAccountDTO.getSavingsProdNameEn());

                            dpsClosure.setStatus(Status.STATUS_PENDING_APPROVAL.getValue());
                            dpsClosure.setOfficeId(command.getOfficeId());
                            dpsClosure.setManagementProcessId(managementProcessId);
                            dpsClosure.setSavingsTypeId(dpsAccountDTO.getSavingsTypeId());

                            return dpsClosure;
                        });
    }

    Mono<DPSClosure> getTotalInterestAndMaturityAmountForDPSClosureV2(PassbookResponseDTO lastPassbookEntry, DPSAccountDTO dpsAccount, DPSClosureCommand command) {
        /*
         * check if passbookResponseDTO transaction code is INTEREST_DEPOSIT
         *       calculate interest
         *
         * else get last Interest Deposit entry
         *       get transaction date
         *       get passbook entries in between closing date
         *       calculate interest
         *
         */

        return Mono.just(lastPassbookEntry)
                .flatMap(passbookResponseDTO -> passbookResponseDTO.getTransactionCode().equalsIgnoreCase(TRANSACTION_CODE_INTEREST_DEPOSIT.getValue())
                        ? Mono.just(calculateInterestWhenLastPassbookEntryIsInterestPosting(lastPassbookEntry, dpsAccount, command))
                        : passbookUseCase
                        .getLastInterestDepositPassbookEntryBySavingsAccountOid(dpsAccount.getSavingsAccountOid())
                        .switchIfEmpty(Mono.just(lastPassbookEntry))
                        .map(PassbookResponseDTO::getTransactionDate)
                        .flatMap(lastInterestPostingDate -> passbookUseCase.getPassbookEntriesBetweenTransactionDates(command.getSavingsAccountId(), lastInterestPostingDate, command.getClosingDate()))
                        .flatMap(passbookList -> Mono.just(calculateInterestWhenPassbookEntryExistsAfterLastInterestPosting(passbookList, dpsAccount, command))))
                .map(totalInterestAfterLastInterestPosting ->
                        DPSClosure
                                .builder()
                                .closingInterest(totalInterestAfterLastInterestPosting)
                                .totalInterest(lastPassbookEntry.getTotalAccruedInterDeposit() == null
                                        ? totalInterestAfterLastInterestPosting
                                        : lastPassbookEntry.getTotalAccruedInterDeposit().add(totalInterestAfterLastInterestPosting))
                                .closingAmount(lastPassbookEntry.getSavgAcctEndingBalance().add(totalInterestAfterLastInterestPosting))
                                .actualInterestRate(dpsAccount.getInterestRate())
                                .effectiveInterestRate(getEffectiveInterestRateForDPS(command, dpsAccount))
                                .interestRateFrequency(dpsAccount.getInterestRateFrequency())
                                .interestPostingPeriod(dpsAccount.getInterestPostingPeriod())
                                .interestCompoundingPeriod(dpsAccount.getInterestCompoundingPeriod())
                                .build());
    }


    Mono<DPSClosure> getTotalInterestAndMaturityAmountForDPSClosureInfo(PassbookResponseDTO lastPassbookEntry, DPSAccountDTO dpsAccount, DPSClosureCommand command) {
        /*
         * check if passbookResponseDTO transaction code is INTEREST_DEPOSIT
         *       calculate interest
         *
         * else get last Interest Deposit entry
         *       get transaction date
         *       get passbook entries in between closing date
         *       calculate interest
         *
         */

        return Mono.just(lastPassbookEntry)
                .flatMap(passbookResponseDTO -> passbookResponseDTO.getTransactionCode().equalsIgnoreCase(TRANSACTION_CODE_INTEREST_DEPOSIT.getValue())
                        ? Mono.just(calculateInterestWhenLastPassbookEntryIsInterestPosting(lastPassbookEntry, dpsAccount, command))
                        : passbookUseCase
                        .getLastInterestDepositPassbookEntryBySavingsAccountOid(dpsAccount.getSavingsAccountOid())
                        .switchIfEmpty(Mono.just(lastPassbookEntry))
                        .map(PassbookResponseDTO::getTransactionDate)
                        .flatMap(lastInterestPostingDate -> passbookUseCase.getPassbookEntriesBetweenTransactionDates(command.getSavingsAccountId(), lastInterestPostingDate, command.getClosingDate()))
                        .flatMap(passbookList -> Mono.just(calculateInterestWhenPassbookEntryExistsAfterLastInterestPosting(passbookList, dpsAccount, command))))
                .map(totalInterestAfterLastInterestPosting ->
                        DPSClosure
                                .builder()
                                .closingInterest(totalInterestAfterLastInterestPosting)
                                .totalInterest(lastPassbookEntry.getTotalAccruedInterDeposit() == null
                                        ? totalInterestAfterLastInterestPosting
                                        : lastPassbookEntry.getTotalAccruedInterDeposit().add(totalInterestAfterLastInterestPosting))
                                .closingAmount(lastPassbookEntry.getSavgAcctEndingBalance().add(totalInterestAfterLastInterestPosting))
                                .actualInterestRate(dpsAccount.getInterestRate())
                                .effectiveInterestRate(getEffectiveInterestRateForDPS(command, dpsAccount))
                                .interestRateFrequency(dpsAccount.getInterestRateFrequency())
                                .interestPostingPeriod(dpsAccount.getInterestPostingPeriod())
                                .interestCompoundingPeriod(dpsAccount.getInterestCompoundingPeriod())
                                .build());
    }


    Mono<DPSClosure> getTotalInterestAndMaturityAmountForDPSClosureWhenMaturityAmountProvided(PassbookResponseDTO lastPassbookEntry, DPSAccountDTO dpsAccount, DPSClosureCommand command) {

        return Mono.just(lastPassbookEntry)
                .map(lastPassbookEnry ->
                        DPSClosure
                                .builder()
                                .closingInterest(lastPassbookEnry.getTotalDepositAmount() != null ? command.getMaturityAmount().subtract(lastPassbookEnry.getTotalDepositAmount()) : BigDecimal.ZERO)
                                .totalInterest(lastPassbookEnry.getTotalDepositAmount() != null ? command.getMaturityAmount().subtract(lastPassbookEnry.getTotalDepositAmount()) : lastPassbookEnry.getTotalAccruedInterDeposit())
                                .closingAmount(command.getMaturityAmount())
                                .actualInterestRate(dpsAccount.getInterestRate())
                                .effectiveInterestRate(getEffectiveInterestRateForDPS(command, dpsAccount))
                                .interestRateFrequency(dpsAccount.getInterestRateFrequency())
                                .interestPostingPeriod(dpsAccount.getInterestPostingPeriod())
                                .interestCompoundingPeriod(dpsAccount.getInterestCompoundingPeriod())
                                .build());
    }

    BigDecimal calculateInterestWhenLastPassbookEntryIsInterestPosting(PassbookResponseDTO lastPassbookEntry, DPSAccountDTO dpsAccountDTO, DPSClosureCommand command) {
        BigDecimal effectiveInterestRate = getEffectiveInterestRateForDPS(command, dpsAccountDTO);
        BigDecimal effectiveInterestRatePerDay = effectiveInterestRate.divide(BigDecimal.valueOf(36500), 8,RoundingMode.UP);
        BigDecimal savingsAmount = lastPassbookEntry.getSavgAcctEndingBalance();

        log.info("lastPassbookEntry : {}", lastPassbookEntry);
        log.info("dpsAccountDTO : {}", dpsAccountDTO);
        log.info("effectiveInterestRate : {}", effectiveInterestRate);
        log.info("savgAcctEndingBalance : {}", savingsAmount);

        long numberOfDays = Math.abs(ChronoUnit.DAYS.between(command.getClosingDate(), lastPassbookEntry.getTransactionDate()));
        log.info("numberOfDays: {}", numberOfDays);

        BigDecimal interestCalculatedFromLastPostingUpToClosing = CommonFunctions.round(savingsAmount
                        .multiply(effectiveInterestRatePerDay)
                        .multiply(BigDecimal.valueOf(numberOfDays)),
                2,
                RoundingMode.HALF_UP);
        log.info("interestCalculatedFromLastPostingUpToClosing : {}", interestCalculatedFromLastPostingUpToClosing);

        return interestCalculatedFromLastPostingUpToClosing;
    }


    BigDecimal calculateInterestWhenPassbookEntryExistsAfterLastInterestPosting(List<PassbookResponseDTO> passbookList, DPSAccountDTO dpsAccountDTO, DPSClosureCommand command) {
        List<LocalDate> transactionDates = new ArrayList<>(passbookList.stream().map(PassbookResponseDTO::getTransactionDate).sorted().toList());
        transactionDates.add(command.getClosingDate());
//        passbookList.sort(Comparator.comparing(PassbookResponseDTO::getTransactionDate));
        BigDecimal effectiveInterestRate = getEffectiveInterestRateForDPS(command, dpsAccountDTO);
        BigDecimal effectiveInterestRatePerDay = effectiveInterestRate.divide(BigDecimal.valueOf(36500), 8,RoundingMode.UP);
        BigDecimal totalInterest = BigDecimal.ZERO;
        log.info("passbookList size : {}", passbookList.size());

        for (int i = 0; i < transactionDates.size()-1; i++) {
            LocalDate currentTransactionDate = transactionDates.get(i);
            LocalDate nextTransactionDate = transactionDates.get(i+1);
            long numberOfDays = Math.abs(ChronoUnit.DAYS.between(currentTransactionDate, nextTransactionDate));
            log.info("number of Days between {} & {} = {}", currentTransactionDate, nextTransactionDate, numberOfDays);
            BigDecimal savgAcctEndingBalanceBeforeNextTransaction = passbookList.get(i).getSavgAcctEndingBalance();
            log.info("savgAcctEndingBalanceBeforeNextTransaction : {}", savgAcctEndingBalanceBeforeNextTransaction);
            BigDecimal interestForThisPeriod = savgAcctEndingBalanceBeforeNextTransaction
                    .multiply(effectiveInterestRatePerDay)
                    .multiply(BigDecimal.valueOf(numberOfDays));
            log.info("interestForThisPeriod : {}", interestForThisPeriod);
            totalInterest = totalInterest.add(interestForThisPeriod);
            log.info("totalInterest : {}", totalInterest);
        }

        if (passbookList.size() == 1) {
            log.info("passbook list size is 1 & calculating total interest");
            long numberOfDays = Math.abs(ChronoUnit.DAYS.between(passbookList.get(0).getTransactionDate(), command.getClosingDate()));
            log.info("number of Days between {} & {} = {}", passbookList.get(0).getTransactionDate(), command.getClosingDate(), numberOfDays);
            totalInterest = passbookList.get(0).getSavgAcctEndingBalance()
                    .multiply(effectiveInterestRatePerDay)
                    .multiply(BigDecimal.valueOf(numberOfDays));
            log.info("total interest : {}", totalInterest);
        }

        totalInterest = CommonFunctions.round(totalInterest, 2, RoundingMode.HALF_UP);
        log.info("totalInterest : {}", totalInterest);


        return totalInterest;
    }


    BigDecimal getEffectiveInterestRateForDPS(DPSClosureCommand command, DPSAccountDTO dpsAccountDTO) {
        BigDecimal effectiveInterestRate = command.getEffectiveInterestRate() == null
                || command.getClosingDate().isAfter(dpsAccountDTO.getAcctEndDate())
                || command.getClosingDate().isEqual(dpsAccountDTO.getAcctEndDate())
                ? CommonFunctions.getAnnualInterestRate(dpsAccountDTO.getInterestRate(), dpsAccountDTO.getInterestRateFrequency())
                : CommonFunctions.getAnnualInterestRate(command.getEffectiveInterestRate(), dpsAccountDTO.getInterestRateFrequency());

        // if effective interest rate is provided and premature en-cashing DPS account,
        // the provided interest rate cannot be greater than the account interest rate, can be less or equal
        if (command.getEffectiveInterestRate() != null
                && command.getClosingDate().isBefore(dpsAccountDTO.getAcctEndDate())
                && command.getEffectiveInterestRate().compareTo(dpsAccountDTO.getInterestRate()) > 0) {
            effectiveInterestRate = CommonFunctions.getAnnualInterestRate(dpsAccountDTO.getInterestRate(), dpsAccountDTO.getInterestRateFrequency());
        }

        return effectiveInterestRate;
    }

    private DPS buildDPSForDetailView(DPSAccountDTO dpsAccountDTO) {
        return mapper.map(dpsAccountDTO, DPS.class);
    }

    private DPSDetailViewDTO buildDPSDetailViewDTO(DPS dps) {
        return DPSDetailViewDTO
                .builder()
                .userMessage("DPS account detail fetched successfully")
                .data(dps)
                .build();
    }
}
