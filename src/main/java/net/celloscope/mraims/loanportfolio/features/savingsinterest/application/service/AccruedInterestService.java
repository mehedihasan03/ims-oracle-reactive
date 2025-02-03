package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.SavingsAccountProductEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.MetaPropertyResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyEnum;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.AccruedInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.SavingsInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.AccruedInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.CalculateInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.AccruedInterestDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.AccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsAccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.out.AccruedInterestPort;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.AccruedInterestDTODomain;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.CalculateInterestData;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.SavingsAccountInterestDeposit;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.SavingsInterestMetaProperty;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.enums.AccountBalanceCalculationMethod;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.INTEREST_ALREADY_ACCRUED;

@Service
@Slf4j
public class AccruedInterestService implements AccruedInterestUseCase {
    private final SavingsInterestUseCase savingsInterestUseCase;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final AccruedInterestPort port;
    private final ModelMapper modelMapper;
    private final CommonRepository commonRepository;
    private final MetaPropertyUseCase metaPropertyUseCase;
    private final PassbookUseCase passbookUseCase;
    private final SavingsInterestService savingsInterestService;
    private final TransactionUseCase transactionuseCase;
    private final TransactionalOperator rxtx;
    private final Gson gson;

    public AccruedInterestService(SavingsInterestUseCase savingsInterestUseCase, ISavingsAccountUseCase savingsAccountUseCase, AccruedInterestPort port, ModelMapper modelMapper, CommonRepository commonRepository, MetaPropertyUseCase metaPropertyUseCase, PassbookUseCase passbookUseCase, SavingsInterestService savingsInterestService, TransactionUseCase transactionuseCase, TransactionalOperator rxtx) {
        this.savingsInterestUseCase = savingsInterestUseCase;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.port = port;
        this.modelMapper = modelMapper;
        this.commonRepository = commonRepository;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.passbookUseCase = passbookUseCase;
        this.savingsInterestService = savingsInterestService;
        this.transactionuseCase = transactionuseCase;
        this.rxtx = rxtx;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<SavingsAccruedInterestResponseDTO> saveAccruedInterest(AccruedInterestCommand command) {
        return port.checkIfExistsByYearMonthAndSavingsAccountId(command.getInterestCalculationYear(), command.getInterestCalculationMonth(), command.getSavingsAccountId())
                .flatMap(aBoolean -> aBoolean
                        ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, INTEREST_ALREADY_ACCRUED.getValue()))
                        : savingsInterestUseCase.calculateMonthlyAccruedInterest(modelMapper.map(command, CalculateInterestCommand.class)))
                .doOnNext(savingsAccruedInterestResponseDTO -> log.info("Successfully Calculated Monthly Savings Interest : {}", savingsAccruedInterestResponseDTO))
                .flatMap(savingsAccruedInterestResponseDTO -> port.saveAccruedInterest(savingsAccruedInterestResponseDTO, command))
                .map(accruedInterestDTO -> modelMapper.map(accruedInterestDTO, SavingsAccruedInterestResponseDTO.class));
    }

    SavingsAccountInterestDeposit buildDomain(SavingsAccruedInterestResponseDTO savingsAccruedInterestResponseDTO, SavingsAccountResponseDTO savingsAccount, String loginId) {
        return SavingsAccountInterestDeposit
                .builder()
                .savingsAccountId(savingsAccruedInterestResponseDTO.getSavingsAccountId())
                .savingsAccountOid(savingsAccruedInterestResponseDTO.getSavingsAccountOid())
                .memberId(savingsAccruedInterestResponseDTO.getMemberId())
//                .productType(savingsAccount.getSavingsProductType())
                .interestCalculationMonth(savingsAccruedInterestResponseDTO.getInterestCalculationMonth())
                .accruedInterestAmount(savingsAccruedInterestResponseDTO.getAccruedInterestAmount())
                .fromDate(savingsAccruedInterestResponseDTO.getFromDate())
                .savgAcctBeginBalance(savingsAccruedInterestResponseDTO.getSavgAcctBeginBalance())
                .toDate(savingsAccruedInterestResponseDTO.getToDate())
                .savgAcctEndingBalance(savingsAccruedInterestResponseDTO.getSavgAcctEndingBalance())
                .createdOn(LocalDateTime.now())
                .createdBy(loginId)
                .status(Status.STATUS_PENDING.getValue())
                .build();
    }

    @Override
    public Flux<SavingsAccruedInterestResponseDTO> getAccruedInterestEntriesBySavingsAccountIdYearAndClosingType(String savingsAccountId, Integer year, String closingType) {
        List<String> monthList = new ArrayList<>();
        if (closingType.equalsIgnoreCase(Constants.CLOSING_TYPE_JUNE_CLOSING.getValue()))
            monthList = List.of(Month.JANUARY.toString(), Month.FEBRUARY.toString(), Month.MARCH.toString(), Month.APRIL.toString(), Month.MAY.toString(), Month.JUNE.toString());
        else if (closingType.equalsIgnoreCase(Constants.CLOSING_TYPE_YEAR_CLOSING.getValue()))
            monthList = List.of(Month.JULY.toString(), Month.AUGUST.toString(), Month.SEPTEMBER.toString(), Month.OCTOBER.toString(), Month.NOVEMBER.toString(), Month.DECEMBER.toString());

        log.info("closing Type : {}", closingType);
        log.info("month List : {}", monthList);
        return port
                .getAccruedInterestEntriesBySavingsAccountIdYearAndMonthListAndStatus(savingsAccountId, year, monthList, Status.STATUS_PENDING.getValue())
                .doOnRequest(l -> log.info("Service | requesting to get entries."))
                .doOnNext(savingsAccruedInterestResponseDTO -> log.info("entry received : {}", savingsAccruedInterestResponseDTO));
    }

    @Override
    public Mono<Boolean> updateTransactionIdAndStatusByAccruedInterestIdList(List<String> accruedInterestIdList, String transactionId, String status) {
        return port
                .updateTransactionIdAndStatusByAccruedInterestIdList(accruedInterestIdList, transactionId, status)
                .doOnRequest(l -> log.info("request received to update transaction id : {} and status : {} for accruedInterestIdList : {}", transactionId, status, accruedInterestIdList));
    }

    @Override
    public Mono<SavingsAccruedInterestResponseDTO> saveFDRAccruedInterest(AccruedInterestCommand command) {
        return null;
    }

    @Override
    public Mono<String> calculateMonthlySavingsInterestAndAccrue(String officeId, Integer interestCalculationMonth, Integer interestCalculationYear, String loginId) {
        return savingsAccountUseCase
                .getSavingsAccountsByOfficeId(officeId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Savings Account Found for Office : " + officeId)))
                .collectList()
                .doOnSuccess(savingsAccountResponseDTOS -> log.info("successfully parsed savings accounts by office : {}", officeId))
                .flatMapMany(Flux::fromIterable)
                .flatMap(savingsAccountResponseDTO -> savingsInterestUseCase
                        .calculateMonthlyAccruedInterest(CalculateInterestCommand
                                                        .builder()
                                                        .savingsAccountId(savingsAccountResponseDTO.getSavingsAccountId())
                                                        .interestCalculationMonth(interestCalculationMonth)
                                                        .interestCalculationYear(interestCalculationYear)
                                                        .build())
                        .doOnError(throwable -> log.info("Error Happened while calculating savings interest for : {}", savingsAccountResponseDTO.getSavingsAccountId()))
                        .map(savingsAccruedInterestResponseDTO -> buildDomain(savingsAccruedInterestResponseDTO, savingsAccountResponseDTO, loginId)))
                .flatMap(savingsAccountInterestDeposit -> port.checkIfExistsByYearMonthAndSavingsAccountId(interestCalculationYear, interestCalculationMonth, savingsAccountInterestDeposit.getSavingsAccountId())
                        .flatMap(aBoolean -> aBoolean
                                ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, INTEREST_ALREADY_ACCRUED.getValue()))
                                : port.saveAccruedInterestV2(savingsAccountInterestDeposit)))
                .collectList()
                .thenReturn("Successful");
    }

    @Override
    public Mono<AccruedInterestResponseDTO> getAccruedInterestEntriesByManagementProcessIdAndOfficeId(String managementProcessId, String officeId) {
        return port
                .getAccruedInterestEntriesByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .doOnError(throwable -> log.error("Error Happened while fetching Accrued Interest Entries with processManagementId : {} & officeId : {} | errorMessage: {}", managementProcessId, officeId, throwable.getMessage()))
                .collectList()
                .map(accruedInterestList ->
                        AccruedInterestResponseDTO
                                .builder()
                                .data(accruedInterestList)
                                .count(accruedInterestList.size())
                                .userMessage("Accrued Interest Data fetched Successfully.")
                                .build());
    }

    private Mono<Integer> deleteIfAlreadyAccruedAndPosted(String managementProcessId, List<String> savingsAccountIdList, String samityId) {
        return rxtx.transactional(
                port.deleteIfAlreadyAccrued(managementProcessId, samityId)
                .doOnNext(totalCount -> log.info("Total Deleted Accrued Interest Entry for Savings Account : {}", totalCount))
                .flatMap(integer -> passbookUseCase.deletePostedInterestBySavingsAccountIdList(managementProcessId, savingsAccountIdList))
                .doOnNext(totalCount -> log.info("Total Deleted Passbook Posted Interest Entry for Savings Account : {}", totalCount))
                .flatMap(integer -> transactionuseCase.deletePostedInterestBySavingsAccountIdList(managementProcessId, savingsAccountIdList))
                .doOnNext(totalCount -> log.info("Total Deleted Transaction Posted Interest Entry for Savings Account : {}", totalCount)));
    }


    private Mono<List<Tuple2<AccruedInterestDTO, CalculateInterestData>>> accrueMonthlyInterest(List<String> savingsAccountIdList, Integer month, Integer year, String loginId, String managementProcessId, String processId, String samityId, String officeId, LocalDate businessDate) {
        AtomicReference<SavingsAccountProductEntity> savingsAccountEntity = new AtomicReference<>(new SavingsAccountProductEntity());
        return this.deleteIfAlreadyAccruedAndPosted(managementProcessId,savingsAccountIdList, samityId)
                .flatMapIterable(totalCount -> savingsAccountIdList)
                .map(savingsAccountId -> CalculateInterestCommand.builder()
                        .interestCalculationMonth(month)
                        .interestCalculationYear(year)
                        .savingsAccountId(savingsAccountId)
                        .build())
                .flatMap(command -> commonRepository
                        .getSavingsProductEntityBySavingsAccountId(command.getSavingsAccountId())
                        .doOnNext(savingsAccountEntity::set)
                        .doOnNext(savingsAccountProductEntity -> log.info("savings product entity : {}", savingsAccountProductEntity))
                        .zipWith(metaPropertyUseCase.getMetaPropertyByPropertyId(MetaPropertyEnum.SAVINGS_INTEREST_META_PROPERTY_ID.getValue()))
                        .flatMap(tuple2 -> this.buildData(tuple2, command, managementProcessId, processId, samityId, officeId, businessDate, loginId))
                        .doOnNext(data -> log.info("Build Data for Savings Account : {}", data))
                        .filter(data -> data.getSavingsAccountOid() != null)
                        .flatMap(data -> passbookUseCase
                                        .getPassbookEntitiesByYearMonthAndSavingsAccountOid(command.getInterestCalculationYear(), command.getInterestCalculationMonth(), data.getSavingsAccountOid())
                                        .doOnNext(passbookResponseDTOList -> log.info("passbook response dto list : {}", passbookResponseDTOList))
                                        .switchIfEmpty(passbookUseCase.getLastPassbookEntryBySavingsAccount(command.getSavingsAccountId()))
                                        .doOnNext(passbookResponseDTO -> log.info("last passbook entry : {}", passbookResponseDTO))
                                        .onErrorReturn(PassbookResponseDTO.builder().passbookNumber(null).build())
                                        .filter(passbookResponseDTO -> passbookResponseDTO.getPassbookNumber() != null)
                                        .doOnError(throwable -> log.error("Error happened while fetching last passbook entry. {}", throwable.getMessage()))
                                        .collectList()
                                        .doOnNext(passbookResponseDTOList -> log.info("passbook response dto list : {}", passbookResponseDTOList))
                                        .flatMap(listOfPassbook -> this.getMonthlyAccruedInterestAccordingToSavingsTypeId(command.getInterestCalculationMonth(), command.getInterestCalculationYear(), listOfPassbook, data))
                                        .filter(accruedInterestDTO -> !HelperUtil.checkIfNullOrEmpty(accruedInterestDTO.getSavingsAccountId()))
                                        .doOnNext(accruedInterestDTO -> log.info("accrued interest dto : {}", accruedInterestDTO))
                                        .map(accruedInterestDTO -> Tuples.of(accruedInterestDTO, data))
//                        todo : posting interest is disabled for now
//                        .flatMap(accruedInterestDTO -> postInterest(accruedInterestDTO, savingsAccountEntity.get(), data, businessDate))
                        ))
                .collectList();
    }


    private SavingsAccountInterestDeposit buildSavingsAccountInterestDeposit(AccruedInterestDTO accruedInterestDTO, CalculateInterestData data) {
        return SavingsAccountInterestDeposit
                .builder()
                .accruedInterestId(UUID.randomUUID().toString())
                .savingsAccountId(accruedInterestDTO.getSavingsAccountId())
                .savingsAccountOid(data.getSavingsAccountOid())
                .managementProcessId(data.getManagementProcessId())
                .processId(data.getProcessId())
                .officeId(data.getOfficeId())
                .samityId(data.getSamityId())
                .memberId(data.getMemberId())
                .productId(data.getSavingsProductId())
                .savingsTypeId(data.getSavingsTypeId())
                .interestCalculationMonth(data.getInterestCalculationMonth())
                .interestCalculationYear(data.getInterestCalculationYear())
                .accruedInterestAmount(accruedInterestDTO.getTotalInterestAccrued())
                .savgAcctBeginBalance(accruedInterestDTO.getSavgAcctBeginBalance())
                .savgAcctEndingBalance(accruedInterestDTO.getSavgAcctEndingBalance())
                .fromDate(accruedInterestDTO.getFromDate())
                .toDate(accruedInterestDTO.getToDate())
                .status(Status.STATUS_PENDING.getValue())
                .createdOn(LocalDateTime.now())
                .createdBy(data.getLoginId())
                .build();
    }


    public Mono<AccruedInterestDTO> accrueAndSaveMonthlyInterest(List<String> savingsAccountIdList, Integer month, Integer year, String loginId, String managementProcessId, String processId, String samityId, String officeId, LocalDate businessDate) {
        return this.accrueMonthlyInterest(savingsAccountIdList, month, year, loginId, managementProcessId, processId, samityId, officeId, businessDate)
            .flatMapMany(Flux::fromIterable)
            .map(tuple2 -> {
                AccruedInterestDTO accruedInterestDTO = tuple2.getT1();
                CalculateInterestData data = tuple2.getT2();
                return this.buildSavingsAccountInterestDeposit(accruedInterestDTO, data);
                })
            .flatMap(port::saveAccruedInterestV2)
            .collectList()
            .map(savingsAccountInterestDeposits -> {
                    Integer totalAccruedAccount = savingsAccountInterestDeposits.size();
                    BigDecimal totalInterestAccrued = savingsAccountInterestDeposits.stream()
                            .filter(savingsAccountInterestDeposit -> savingsAccountInterestDeposit.getAccruedInterestAmount() != null)
                            .map(SavingsAccountInterestDeposit::getAccruedInterestAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    /*BigDecimal totalInterestPosted = accruedInterestDTOS.stream()
                            .filter(accruedInterestDTO -> accruedInterestDTO.getTotalInterestPosted() != null)
                            .map(AccruedInterestDTO::getTotalInterestPosted).reduce(BigDecimal.ZERO, BigDecimal::add);
//                            BigDecimal totalInterestPosted = BigDecimal.ZERO;*/
                    return AccruedInterestDTO
                            .builder()
                            .totalAccruedAccount(totalAccruedAccount)
                            .totalInterestAccrued(totalInterestAccrued)
//                                    .totalInterestPosted(totalInterestPosted)
                            .totalInterestPosted(BigDecimal.ZERO)
                            .build();
            })
                .doOnNext(accruedInterestDTO -> log.info("built accrued interest dto :{}", accruedInterestDTO));
    }

    @Override
    public Mono<List<AccruedInterestDTO>> calculateMonthlyAccruedInterest(String savingsAccountId, Integer month, Integer year, LocalDate businessDate) {
        return commonRepository.getMemberSamityOfficeInfoBySavingsAccountId(savingsAccountId)
                .flatMap(memberSamityOfficeEntity ->
                        this.accrueMonthlyInterest(List.of(savingsAccountId), month, year, "loginId",
                                        memberSamityOfficeEntity.getManagementProcessId(), "processId",
                                        memberSamityOfficeEntity.getSamityId(), memberSamityOfficeEntity.getOfficeId(), businessDate))
                .flatMapMany(Flux::fromIterable)
                .map(Tuple2::getT1)
                .collectList();
    }


    private Mono<AccruedInterestDTO> postInterest(AccruedInterestDTO accruedInterestDTO, SavingsAccountProductEntity savingsAccountProductEntity, CalculateInterestData data, LocalDate businessDate, String officeId) {
        log.info("Posting Interest for Savings Account : {}", accruedInterestDTO.getSavingsAccountId());
        String savingsAccountId = accruedInterestDTO.getSavingsAccountId();
        String savingsTypeId = accruedInterestDTO.getSavingsTypeId();
        YearMonth targetYearMonth = YearMonth.of(accruedInterestDTO.getYear(), accruedInterestDTO.getMonth());
//        Gson gson = new Gson();
        List<LocalDate> interestPostingdatesForThisYearMonth = new ArrayList<>();
        log.info("Savings Account Product Entity Interest Posting Dates: {}", savingsAccountProductEntity.getInterestPostingDates());
        if(!HelperUtil.checkIfNullOrEmpty(savingsAccountProductEntity.getInterestPostingDates())){
            List<String> listOfDates = gson.fromJson(savingsAccountProductEntity.getInterestPostingDates(), ArrayList.class);
        interestPostingdatesForThisYearMonth = listOfDates.stream()
                .map(LocalDate::parse)
                .peek(date -> log.info("Interest Posting Date : {}, Year Month: {}, Target Year Month: {}", date, YearMonth.from(date), targetYearMonth))
                .filter(date -> YearMonth.from(date).equals(targetYearMonth))
                .toList();
        }



        log.info("Interest Posting Dates for this Year Month : {}", interestPostingdatesForThisYearMonth);
        Mono<AccruedInterestDTO> accruedInterestDTOMono = Mono.just(accruedInterestDTO);



        if ((savingsTypeId.equalsIgnoreCase("DPS") || savingsTypeId.equalsIgnoreCase("FDR")) && !interestPostingdatesForThisYearMonth.isEmpty()) {
                accruedInterestDTO.setTotalInterestPosted(BigDecimal.ZERO);
                return accruedInterestDTOMono;
              /*
              * *//** get last interest posting date
                * if empty, get account opening date
                * calculate interest for this time period
                * create transaction
                * create passbook entry
                * *//*

                LocalDate interestPostingDate = interestPostingdatesForThisYearMonth.get(0);

                accruedInterestDTOMono = passbookUseCase
                        .getPassbookEntriesByTransactionCodeAndSavingsAccountId(Constants.TRANSACTION_CODE_INTEREST_DEPOSIT.getValue(), savingsAccountId)
                        .filter(list -> !list.isEmpty())
                        .switchIfEmpty(savingsAccountUseCase
                                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                                .map(SavingsAccountResponseDTO::getAcctStartDate)
                                .map(acctStartDate -> List.of(PassbookResponseDTO.builder().transactionDate(acctStartDate).build())))
                        .map(passbookResponseDTOS -> passbookResponseDTOS
                                .stream().max(Comparator.comparing(PassbookResponseDTO::getTransactionDate)).get().getTransactionDate())
                        .flatMap(lastInterestPostingDate -> savingsInterestUseCase.calculateInterestBetweenDates(lastInterestPostingDate, interestPostingDate, savingsAccountId, businessDate))
                        .flatMap(interestAmount -> transactionuseCase
                                        .createTransactionForSavingsInterestDeposit(data.getLoginId(), savingsAccountId, interestPostingDate, interestAmount)
                                        .flatMap(transactionResponseDTO -> passbookUseCase
                                                .createPassbookEntryForInterestDeposit(buildPassbookRequestDTOForSavingsInterestDeposit(transactionResponseDTO)))
                                .thenReturn(interestAmount)
                                .map(interestPosted -> {
                                    accruedInterestDTO.setTotalInterestPosted(interestPosted);
                                    return accruedInterestDTO;
                                }));
                    */
        } else {
            List<Integer> interestPostingMonthsForGSVS = CommonFunctions.getInterestPostingMonthListByInterestPostingPeriod(savingsAccountProductEntity.getInterestPostingPeriod());
            Integer month = targetYearMonth.getMonthValue();
//            LocalDate interestPostingDate = interestPostingdatesForThisYearMonth.get(0);
            LocalDate interestPostingDate = businessDate;

            log.info("Savings Account Id: {}, Interest Posting Months for GSVS : {}, Interest {Posting Date: {}, Target Month: {}", savingsAccountId, interestPostingMonthsForGSVS, interestPostingDate, month);
//            log.info("Month : {}", month);

            if (interestPostingMonthsForGSVS.contains(month)) {
                accruedInterestDTOMono = passbookUseCase
                        .getPassbookEntriesByTransactionCodeAndSavingsAccountId(Constants.TRANSACTION_CODE_INTEREST_DEPOSIT.getValue(), savingsAccountId)
                        .doOnNext(passbookResponseDTOS -> log.info("Passbook Entries for Interest Deposit, total entity list size: {}", passbookResponseDTOS.size()))
                        .filter(list -> !list.isEmpty())
                        .switchIfEmpty(savingsAccountUseCase
                                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                                .map(SavingsAccountResponseDTO::getAcctStartDate)
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Account Start Date Found for Savings Account : " + savingsAccountId)))
                                .map(acctStartDate -> List.of(PassbookResponseDTO.builder().transactionDate(acctStartDate).build())))
                        .map(passbookResponseDTOS -> passbookResponseDTOS
                                .stream().max(Comparator.comparing(PassbookResponseDTO::getTransactionDate)).get().getTransactionDate())
                        .doOnNext(lastInterestPostingDate -> log.info("Last Interest Posting Date : {}", lastInterestPostingDate))
                        .flatMap(lastInterestPostingDate -> savingsInterestUseCase.calculateInterestBetweenDates(lastInterestPostingDate, interestPostingDate, savingsAccountId, businessDate))
                        .flatMap(interestAmount -> transactionuseCase
                                .createTransactionForSavingsInterestDeposit(data.getLoginId(), savingsAccountId, interestPostingDate, interestAmount, officeId)
                                .flatMap(transactionResponseDTO -> passbookUseCase
                                        .createPassbookEntryForInterestDeposit(buildPassbookRequestDTOForSavingsInterestDeposit(transactionResponseDTO)))
                                .thenReturn(interestAmount)
                                .map(interestPosted -> {
                                    accruedInterestDTO.setTotalInterestPosted(interestPosted);
                                    return accruedInterestDTO;
                                }));
            }
            return accruedInterestDTOMono;
        }
//        return accruedInterestDTOMono;
    }

    private PassbookRequestDTO buildPassbookRequestDTOForSavingsInterestDeposit(Transaction transaction) {
        return PassbookRequestDTO
                .builder()
                .processId(transaction.getProcessId())
                .managementProcessId(transaction.getManagementProcessId())
                .paymentMode(transaction.getPaymentMode())
                .savingsAccountId(transaction.getSavingsAccountId())
                .transactionCode(transaction.getTransactionCode())
                .transactionDate(transaction.getTransactionDate())
                .transactionId(transaction.getTransactionId())
                .mfiId(transaction.getMfiId())
                .memberId(transaction.getMemberId())
                .amount(transaction.getAmount())
                .loginId(transaction.getCreatedBy())
                .officeId(transaction.getOfficeId())
                .build();
    }




    private Mono<AccruedInterestDTO> getMonthlyAccruedInterestAccordingToSavingsTypeId(Integer interestCalculationMonth, Integer interestCalculationYear, List<PassbookResponseDTO> listOfPassbook, CalculateInterestData data) {
        Mono<AccruedInterestDTO> accruedInterestDTO = Mono.just(AccruedInterestDTO.builder().build());
        Mono<AccruedInterestDTODomain> accruedInterestDTODomainMono;

//        return savingsInterestService.getMonthlyAccruedInterestForGsVs(interestCalculationMonth, interestCalculationYear, listOfPassbook, data)
//                .map(accruedInterestDTODomain -> AccruedInterestDTO
//                        .builder()
//                        .savingsAccountId(data.getSavingsAccountId())
//                        .savingsAccountOid(data.getSavingsAccountOid())
//                        .savingsProductId(data.getSavingsProductId())
//                        .savingsTypeId(data.getSavingsTypeId())
//                        .yearMonth(YearMonth.of(interestCalculationYear, interestCalculationMonth))
//                        .totalInterestAccrued(accruedInterestDTODomain.getAccruedInterestAmount())
//                        .build());

        String accountBalanceCalculationMethod = data.getAccountBalanceCalculationMethod();

        if (data.getSavingsTypeId().equalsIgnoreCase("GS") || data.getSavingsTypeId().equalsIgnoreCase("VS")) {
            log.info("GS/VS");
            if(!listOfPassbook.isEmpty()){
                accruedInterestDTODomainMono = accountBalanceCalculationMethod.equals(AccountBalanceCalculationMethod.ACCOUNT_BALANCE_CALCULATION_METHOD_MONTHLY_OPEN_END_BASIS.getValue())
                        ? savingsInterestService.getMonthlyAccruedInterestAccordingToMonthlyOpeningEndingBalanceMethod(interestCalculationMonth, interestCalculationYear, listOfPassbook, data)
                        : savingsInterestService.getMonthlyAccruedInterestForGsVs(interestCalculationMonth, interestCalculationYear, listOfPassbook, data);

                accruedInterestDTO = accruedInterestDTODomainMono
                        .map(accruedInterestDTODomain -> this.buildAccruedInterestDTO(accruedInterestDTODomain, interestCalculationMonth, interestCalculationYear, data));
            }
        }

        if (data.getSavingsTypeId().equalsIgnoreCase("DPS") || data.getSavingsTypeId().equalsIgnoreCase("FDR")) {
            log.info("DPS/FDR");
            if(!listOfPassbook.isEmpty()){
                accruedInterestDTODomainMono = accountBalanceCalculationMethod.equals(AccountBalanceCalculationMethod.ACCOUNT_BALANCE_CALCULATION_METHOD_MONTHLY_OPEN_END_BASIS.getValue())
                        ? savingsInterestService.getMonthlyAccruedInterestAccordingToMonthlyOpeningEndingBalanceMethod(interestCalculationMonth, interestCalculationYear, listOfPassbook, data)
                        : savingsInterestService.getMonthlyAccruedInterestForDPS(interestCalculationMonth, interestCalculationYear, listOfPassbook, data);

                accruedInterestDTO = accruedInterestDTODomainMono
                        .map(accruedInterestDTODomain -> this.buildAccruedInterestDTO(accruedInterestDTODomain, interestCalculationMonth, interestCalculationYear, data));
            }
        }

        return accruedInterestDTO;
    }


    private AccruedInterestDTO buildAccruedInterestDTO(AccruedInterestDTODomain accruedInterestDTODomain, Integer interestCalculationMonth, Integer interestCalculationYear, CalculateInterestData data) {
        AccruedInterestDTO accruedInterestDTO = modelMapper.map(accruedInterestDTODomain, AccruedInterestDTO.class);
        accruedInterestDTO.setYear(interestCalculationYear);
        accruedInterestDTO.setMonth(interestCalculationMonth);
        accruedInterestDTO.setTotalInterestAccrued(accruedInterestDTODomain.getAccruedInterestAmount());


        return accruedInterestDTO;
        /*return AccruedInterestDTO
                .builder()
                .savingsAccountId(data.getSavingsAccountId())
                .savingsAccountOid(data.getSavingsAccountOid())
                .savingsProductId(data.getSavingsProductId())
                .savingsTypeId(data.getSavingsTypeId())
                .year(interestCalculationYear)
                .month(interestCalculationMonth)
//                            .yearMonth(YearMonth.of(interestCalculationYear, interestCalculationMonth))
                .totalInterestAccrued(accruedInterestDTODomain.getAccruedInterestAmount())
                .build();*/
    }


    private Mono<CalculateInterestData> buildData(Tuple2<SavingsAccountProductEntity, MetaPropertyResponseDTO> tuple2, CalculateInterestCommand command, String managementProcessId, String processId, String samityId, String officeId, LocalDate businessDate, String loginId) {
        SavingsAccountProductEntity savingsAccountProductEntity = tuple2.getT1();
        MetaPropertyResponseDTO metaPropertyResponseDTO = tuple2.getT2();
        Gson gson = new Gson();
        SavingsInterestMetaProperty metaProperty;

        if (metaPropertyResponseDTO.getParameters() != null) {
            metaProperty = gson.fromJson(metaPropertyResponseDTO.getParameters(), SavingsInterestMetaProperty.class);
        } else
            metaProperty = SavingsInterestMetaProperty
                    .builder()
                    .accruedInterestPrecision(2)
                    .interestRatePrecision(8)
                    .daysInYear("365")
                    .roundingLogic(RoundingMode.HALF_UP.toString())
                    .accountBalanceCalculationMethod(AccountBalanceCalculationMethod.ACCOUNT_BALANCE_CALCULATION_METHOD_DAILY_BASIS.getValue())
                    .build();

        log.info("Meta Property : {}", metaProperty);
        LocalDate accountOpeningDate = savingsAccountProductEntity.getAcctStartDate();
        LocalDate firstDayOfInterestCalculationMonth = LocalDate.of(command.getInterestCalculationYear(), command.getInterestCalculationMonth(), 1);

        if (YearMonth.from(firstDayOfInterestCalculationMonth).isBefore(YearMonth.from(accountOpeningDate)))
            return Mono.just(CalculateInterestData
                    .builder()
                    .savingsAccountOid(null)
                    .build());

        return Mono.just(CalculateInterestData
                .builder()
                .acctStartDate(savingsAccountProductEntity.getAcctStartDate())
                .interestRateFrequency(savingsAccountProductEntity.getInterestRateFrequency())
                .interestRatePrecision(metaProperty.getInterestRatePrecision())
                .accruedInterestPrecision(metaProperty.getAccruedInterestPrecision())
                .roundingMode(CommonFunctions.getRoundingMode(metaProperty.getRoundingLogic()))
                .daysInYear(metaProperty.getDaysInYear())
                .balanceCalculationMethod(savingsAccountProductEntity.getInterestCalculatedUsing())
                .accountBalanceCalculationMethod(metaProperty.getAccountBalanceCalculationMethod())
                .interestPostingPeriod(savingsAccountProductEntity.getInterestPostingPeriod())
                .interestCompoundingPeriod(savingsAccountProductEntity.getInterestCompoundingPeriod())
                .savingsAccountId(command.getSavingsAccountId())
                .interestRate(savingsAccountProductEntity.getInterestRate())
                .provisionInterestRate(savingsAccountProductEntity.getProvisionInterestRate())
                .interestCalculationDate(command.getInterestCalculationDate())
                .interestCalculationMonth(command.getInterestCalculationMonth())
                .interestCalculationYear(command.getInterestCalculationYear())
                .balanceRequiredInterestCalc(BigDecimal.valueOf(Double.parseDouble(savingsAccountProductEntity.getBalanceRequiredInterestCalc())))
                .mfiId(savingsAccountProductEntity.getMfiId())
                .memberId(savingsAccountProductEntity.getMemberId())
                .savingsAccountOid(savingsAccountProductEntity.getSavingsAccountOid())
                .savingsProductId(savingsAccountProductEntity.getSavingsProductId())
                .savingsTypeId(savingsAccountProductEntity.getSavingsTypeId())
                .managementProcessId(managementProcessId)
                .processId(processId)
                .officeId(officeId)
                .samityId(samityId)
                .businessDate(businessDate)
                .loginId(loginId)
                .build());
    }
}
