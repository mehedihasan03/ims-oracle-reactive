package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.SavingsBalanceCalculationMethods;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.SavingsAccountProductEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto.DPSAuthorizeCommand;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.MetaPropertyResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyEnum;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.DpsRepaymentDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.DpsRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.AccruedInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.SavingsInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.AccruedInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.CalculateInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.PostSavingsInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsAccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.SavingsInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.out.AccruedInterestPort;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.*;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.enums.AccountBalanceCalculationMethod;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.enums.DaysInYear;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.TRANSACTION_CODE_INTEREST_DEPOSIT;

@Service
@Slf4j
public class SavingsInterestService implements SavingsInterestUseCase {

    private final PassbookUseCase passbookUseCase;
    private final ISavingsInterestCommands savingsInterestCommands;
    private final CommonRepository commonRepository;
    private final MetaPropertyUseCase metaPropertyUseCase;
    private final AccruedInterestPort accruedInterestPort;
    private final DpsRepaymentScheduleUseCase dpsRepaymentScheduleUseCase;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final TransactionUseCase transactionUseCase;
    private final ModelMapper modelMapper;

    public SavingsInterestService(PassbookUseCase passbookUseCase, ISavingsInterestCommands savingsInterestCommands, CommonRepository commonRepository, MetaPropertyUseCase metaPropertyUseCase, AccruedInterestPort accruedInterestPort, DpsRepaymentScheduleUseCase dpsRepaymentScheduleUseCase, ISavingsAccountUseCase savingsAccountUseCase, TransactionUseCase transactionUseCase, ModelMapper modelMapper) {
        this.passbookUseCase = passbookUseCase;
        this.savingsInterestCommands = savingsInterestCommands;
        this.commonRepository = commonRepository;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.accruedInterestPort = accruedInterestPort;
        this.dpsRepaymentScheduleUseCase = dpsRepaymentScheduleUseCase;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.transactionUseCase = transactionUseCase;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<SavingsInterestResponseDTO> calculateDailyAccruedInterest(CalculateInterestCommand command) {

        return commonRepository
                .getSavingsProductEntityBySavingsAccountId(command.getSavingsAccountId())
                .doOnNext(savingsAccountProductEntity -> log.info("savings product entity : {}", savingsAccountProductEntity))
                .zipWith(metaPropertyUseCase.getMetaPropertyByPropertyId(MetaPropertyEnum.SAVINGS_INTEREST_META_PROPERTY_ID.getValue()))
                .flatMap(tuple2 -> this.buildData(tuple2, command))
                .flatMap(data ->
                        passbookUseCase
                            .getPassbookEntriesBySavingsAccountIDAndTransactionDateOrderByCreatedOn(command.getSavingsAccountId(), command.getInterestCalculationDate())
                            .switchIfEmpty(passbookUseCase
                                    .getLastPassbookEntryBySavingsAccount(command.getSavingsAccountId())
                                    .doOnRequest(l -> log.info("No passbook entry found for date : {}", command.getInterestCalculationDate()))
                                    .doOnRequest(l -> log.info("Requesting to Get last passbook entry for savings account : {}", command.getSavingsAccountId()))
                                    .doOnNext(passbookResponseDTO -> log.info("Last Passbook Entry received : {}", passbookResponseDTO)))
                            .doOnError(throwable -> log.error("Error happened while fetching last passbook entry. {}", throwable.getMessage()))
                            .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Passbook Entry Found")))
                            .collectList()
                            .flatMap(passbookList -> getSavingsBalanceMonoAccordingToCalculationMethod(passbookList, data.getBalanceCalculationMethod(), data.getRoundingMode()))
                            .doOnNext(bigDecimal -> log.info("savings balance on which interest to be calculated : {}", bigDecimal))
                            .map(savingsBalance -> Tuples.of(savingsBalance, savingsInterestCommands.calculateDailyAccruedInterest(savingsBalance, data.getInterestRate(), data.getInterestRateFrequency(), data.getInterestRatePrecision(), data.getAccruedInterestPrecision(), data.getRoundingMode(), data.getDaysInYear(), data.getInterestCalculationDate()))))
                .map(tuple2 -> buildResponse(tuple2, command));

    }

    @Override
    public Mono<SavingsAccruedInterestResponseDTO> calculateMonthlyAccruedInterest(CalculateInterestCommand command) {

        return commonRepository
                .getSavingsProductEntityBySavingsAccountId(command.getSavingsAccountId())
                .doOnNext(savingsAccountProductEntity -> log.info("savings product entity : {}", savingsAccountProductEntity))
                .zipWith(metaPropertyUseCase.getMetaPropertyByPropertyId(MetaPropertyEnum.SAVINGS_INTEREST_META_PROPERTY_ID.getValue()))
                .flatMap(tuple2 -> this.buildData(tuple2, command))
                .filter(data -> data.getSavingsAccountOid() != null)
                .flatMap(data -> passbookUseCase
                        .getPassbookEntitiesByYearMonthAndSavingsAccountOid(command.getInterestCalculationYear(), command.getInterestCalculationMonth(), data.getSavingsAccountOid())
                        .switchIfEmpty(passbookUseCase.getLastPassbookEntryBySavingsAccount(command.getSavingsAccountId()))
                        /*.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Savings Record Found")))*/
                        .onErrorReturn(PassbookResponseDTO.builder().passbookNumber(null).build())
                        .filter(passbookResponseDTO -> passbookResponseDTO.getPassbookNumber() != null)
                        .doOnError(throwable -> log.error("Error happened while fetching last passbook entry. {}", throwable.getMessage()))
                        .collectList()
                        .flatMap(listOfPassbook -> this.getMonthlyAccruedInterestForGsVs(command.getInterestCalculationMonth(), command.getInterestCalculationYear(), listOfPassbook, data))
                        .doOnNext(accruedInterestDTO -> log.info("built accrued interest dto :{}", accruedInterestDTO))
                .map(accruedInterestDTO -> this.buildResponseForMonthlyAccruedInterest(accruedInterestDTO, data, command)));
    }

    @Override
    public Mono<BigDecimal> calculateInterestBetweenDates(LocalDate fromDate, LocalDate toDate, String savingsAccountId, LocalDate businessDate) {

        return commonRepository
                .getSavingsProductEntityBySavingsAccountId(savingsAccountId)
                .doOnNext(savingsAccountProductEntity -> log.info("savings product entity : {}", savingsAccountProductEntity))
                .zipWith(metaPropertyUseCase.getMetaPropertyByPropertyId(MetaPropertyEnum.SAVINGS_INTEREST_META_PROPERTY_ID.getValue()))
                .flatMap(tuple2 -> this.buildData(tuple2, CalculateInterestCommand
                        .builder()
                        .savingsAccountId(savingsAccountId)
                        .businessDate(businessDate)
                        .build()))
                .filter(data -> data.getSavingsAccountOid() != null)
                .flatMap(data -> passbookUseCase.getPassbookEntriesBetweenTransactionDates(savingsAccountId, fromDate, toDate)
                    .filter(passbookResponseDTOS -> !passbookResponseDTOS.isEmpty())
                        .flatMap(passbookList -> getInterestBetweenDates(passbookList, data)));
    }

    @Override
    public Mono<BigDecimal> calculateDPSMaturityAmountWithoutCompounding(String savingsAccountId) {
        return dpsRepaymentScheduleUseCase
                .getDpsRepaymentScheduleBySavingsAccountId(savingsAccountId)
                .zipWith(savingsAccountUseCase.getDPSAccountDetailsBySavingsAccountId(savingsAccountId))
                .map(tuple2 -> {
                    List<DpsRepaymentDTO> dpsRepaymentDTOList = tuple2.getT1();
                    DPSAccountDTO dpsAccountDTO = tuple2.getT2();
                    BigDecimal totalInterest = BigDecimal.ZERO;

                    List<LocalDate> repaymentDates = new ArrayList<>(dpsRepaymentDTOList
                            .stream()
                            .map(DpsRepaymentDTO::getRepaymentDate)
                            .sorted()
                            .toList());
                    repaymentDates.add(dpsAccountDTO.getAcctEndDate());

                    BigDecimal annualInterestRate = CommonFunctions.getAnnualInterestRate(dpsAccountDTO.getInterestRate(), dpsAccountDTO.getInterestRateFrequency());
                    BigDecimal interestRatePerDay = BigDecimal.valueOf(annualInterestRate.doubleValue() / 100 / 365).setScale(8, RoundingMode.HALF_UP);
                    log.info("interest Rate per Day : {}", interestRatePerDay);

                    int count = 0;
                    for (int i=0; i < repaymentDates.size()-1; i++) {
                        long numberOfDaysInBetween = Math.abs(ChronoUnit.DAYS.between(repaymentDates.get(i), repaymentDates.get(i+1)));
                        double currentBalance = dpsAccountDTO.getSavingsAmount().doubleValue() * (i+1);
                        BigDecimal interestForThisPeriod = BigDecimal.valueOf(currentBalance * interestRatePerDay.doubleValue() * numberOfDaysInBetween).setScale(2, RoundingMode.HALF_UP);
                        totalInterest = totalInterest.add(interestForThisPeriod);
                        log.info("current repayment date : {} | next repayment date : {}", repaymentDates.get(i), repaymentDates.get(i+1));
                        log.info("days in between : {}", numberOfDaysInBetween);
                        log.info("interest for this period : {}", interestForThisPeriod);
                        log.info("total interest amount : {}", totalInterest);
                        count++;
                    }


                    BigDecimal totalDeposit = dpsAccountDTO.getSavingsAmount().multiply(BigDecimal.valueOf(repaymentDates.size()-1));
                    BigDecimal maturityAmount = totalDeposit.add(totalInterest).setScale(0, RoundingMode.HALF_UP);

                    log.info("count : {}", count);
                    log.info("total deposit : {}", totalDeposit);
                    log.info("total interest : {}", totalInterest);
                    log.info("maturity Amount : {}", maturityAmount);
                    return maturityAmount;
                });
    }

    @Override
    public Mono<List<SavingsAccountInterestDeposit>> getAllSavingsAccountInterestDepositsForManagementProcessId(String managementProcessId) {
        return accruedInterestPort
                .findAllSavingsAccountInterestDepositsForManagementProcessId(managementProcessId)
                .doOnRequest(l -> log.info("Request received to get all savings account interest deposits for managementProcessId : {}", managementProcessId))
                .collectList()
                .doOnSuccess(savingsAccountInterestDeposits -> log.info("Successfully fetched all savings account interest deposits for managementProcessId - {}", savingsAccountInterestDeposits))
                .doOnError(throwable -> log.error("Error happened while fetching all savings account interest deposits for managementProcessId - {}", throwable.getMessage()))
                ;
    }

    @Override
    public Mono<String> postSavingsInterest(PostSavingsInterestCommand command) {
        String savingsAccountId = command.getSavingsAccountId();
        BigDecimal interestAmount = command.getInterestAmount();
        LocalDate interestPostingDate = command.getInterestPostingDate();
        String loginId = command.getLoginId();
        String officeId = command.getOfficeId();

        return transactionUseCase
                .createTransactionForSavingsInterestDeposit(loginId, savingsAccountId, interestPostingDate, interestAmount, officeId)
                .map(transaction -> modelMapper.map(transaction, SingleTransactionResponseDTO.class))
                .doOnRequest(l -> log.info("Request received to create transaction for accrued interest with loginId : {}, savingsAccountId : {}, interestCalculationDate : {}, accruedInterestAmount : {}", loginId, savingsAccountId, interestPostingDate, interestAmount))
                .doOnSuccess(transactionResponseDTO -> log.info("Successfully created transaction & ready to send response : {}", transactionResponseDTO))
                .flatMap(transaction -> passbookUseCase.createPassbookEntryForInterestDeposit(this.buildPassbookRequestDTOForInterestPosting(transaction, interestAmount, loginId, officeId)))
                .map(passbookResponseDTO -> "Successfully posted interest for savings account : " + savingsAccountId);
    }


    private PassbookRequestDTO buildPassbookRequestDTOForInterestPosting(SingleTransactionResponseDTO transactionResponseDTO, BigDecimal totalInterest, String loginId, String officeId) {
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
                .loginId(loginId)
                .officeId(officeId)
                .transactionDate(transactionResponseDTO.getTransactionDate())
                .paymentMode(transactionResponseDTO.getPaymentMode())
                .memberId(transactionResponseDTO.getMemberId())
                .build();

        log.info("Passbook Request DTO For Interest posting : {}", requestDTO);
        return requestDTO;
    }

    public Mono<BigDecimal> getInterestBetweenDates(List<PassbookResponseDTO> passbookResponseDTOList, CalculateInterestData data) {


        Map<LocalDate, List<PassbookResponseDTO>> transactionDatePassbookListMap = getTransactionDatePassbookListMap(passbookResponseDTOList);
        Map<LocalDate, BigDecimal> transactionDateBalanceMap = getTransactionDateBalanceMap(transactionDatePassbookListMap, data.getBalanceCalculationMethod(), data.getRoundingMode(), data.getBusinessDate());
        LocalDate firstDateInMap = transactionDateBalanceMap.keySet().stream().sorted().findFirst().orElseThrow(() -> new RuntimeException("No transaction date found"));

        BigDecimal interestRatePerDay = calculateInterestRatePerDay(data);
        List<LocalDate> transactionDatesSorted = transactionDateBalanceMap.keySet().stream().sorted().toList();
        log.info("transactionDates : {}", transactionDatesSorted);
        BigDecimal savingsBalanceAtBeginning = transactionDateBalanceMap.get(firstDateInMap);;
        assert firstDateInMap != null;

        log.info("Map : {}", transactionDateBalanceMap);
        log.info("Pretty Map : {}", new GsonBuilder().setPrettyPrinting().create().toJson(transactionDateBalanceMap));
        BigDecimal totalInterest = BigDecimal.ZERO;

        long totalNumberOfDaysAccrued = 0;

        if (transactionDatesSorted.size() == 1) {
            long noOfDaysInBetweenTransactions = Math.abs(ChronoUnit.DAYS.between(transactionDatesSorted.get(0), transactionDatesSorted.get(0))) + 1;
            totalNumberOfDaysAccrued += noOfDaysInBetweenTransactions;
            log.info("totalNumberOfDaysAccrued : {}", totalNumberOfDaysAccrued);
            log.info("interestRatePerDay : {}", interestRatePerDay);
            BigDecimal presentSavingsBalance = transactionDateBalanceMap.get(transactionDatesSorted.get(0));
            BigDecimal interestPerDayForThisPeriod = presentSavingsBalance.multiply(interestRatePerDay);

            BigDecimal interestPerDayRounded = CommonFunctions.round(interestPerDayForThisPeriod, data.getAccruedInterestPrecision(), data.getRoundingMode());
            BigDecimal interestForThisPeriod = interestPerDayRounded.multiply(BigDecimal.valueOf(noOfDaysInBetweenTransactions));
            interestForThisPeriod = CommonFunctions.round(interestForThisPeriod, data.getAccruedInterestPrecision(), data.getRoundingMode());
            log.info("interestForThisPeriod : i = {} | transactionDate : {} | present savings balance : {} | interest amount : {}", 0, transactionDatesSorted.get(0), presentSavingsBalance, interestForThisPeriod);

            totalInterest = totalInterest.add(interestForThisPeriod);
            log.info("total interest now : {}", totalInterest);

        } else {
            for (int i = 0; i < transactionDatesSorted.size()-1; i++) {
                BigDecimal presentSavingsBalance = (i == 0) ? savingsBalanceAtBeginning : transactionDateBalanceMap.get(transactionDatesSorted.get(i));
                long noOfDaysInBetweenTransactions = Math.abs(ChronoUnit.DAYS.between(transactionDatesSorted.get(i), transactionDatesSorted.get(i + 1)));

                totalNumberOfDaysAccrued += noOfDaysInBetweenTransactions;
                log.info("totalNumberOfDaysAccrued : {}", totalNumberOfDaysAccrued);
                log.info("interestRatePerDay : {}", interestRatePerDay);
                BigDecimal interestPerDayForThisPeriod = presentSavingsBalance.multiply(interestRatePerDay);

                BigDecimal interestPerDayRounded = CommonFunctions.round(interestPerDayForThisPeriod, data.getAccruedInterestPrecision(), data.getRoundingMode());
                BigDecimal interestForThisPeriod = interestPerDayRounded.multiply(BigDecimal.valueOf(noOfDaysInBetweenTransactions));
                interestForThisPeriod = CommonFunctions.round(interestForThisPeriod, data.getAccruedInterestPrecision(), data.getRoundingMode());
                log.info("interestForThisPeriod : i = {} | transactionDate : {} | present savings balance : {} | interest amount : {}", i, transactionDatesSorted.get(i), presentSavingsBalance, interestForThisPeriod);

                totalInterest = totalInterest.add(interestForThisPeriod);
                log.info("total interest now : {}", totalInterest);
            }
        }

        return Mono.just(totalInterest);

    }

    public AccruedInterestDTODomain getMonthlyAccruedInterest(Integer interestCalculationMonth, Integer interestCalculationYear, List<PassbookResponseDTO> passbookResponseDTOList, CalculateInterestData data) {

        log.info("interestCalculationYear : {}", interestCalculationYear);
        log.info("interestCalculationMonth : {}", interestCalculationMonth);

        LocalDate acctStartDate = data.getAcctStartDate();
        int startDay = acctStartDate.getMonthValue() == interestCalculationMonth ? acctStartDate.getDayOfMonth() : 1;
        YearMonth yearMonth = YearMonth.of(interestCalculationYear, interestCalculationMonth);
        LocalDate startDate = LocalDate.of(interestCalculationYear, interestCalculationMonth, startDay);
        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate endDate = LocalDate.of(interestCalculationYear, interestCalculationMonth, daysInMonth);
        int daysCount = 0;
        BigDecimal totalAccruedInterest = BigDecimal.ZERO;
        LocalDate firstPassbookEntryTransactionDate = passbookResponseDTOList.get(0).getTransactionDate();

        BigDecimal lastEndingBalance = firstPassbookEntryTransactionDate.equals(yearMonth.atDay(1)) || firstPassbookEntryTransactionDate.equals(acctStartDate)
                ? passbookResponseDTOList.get(0).getSavgAcctEndingBalance()
                : passbookResponseDTOList.get(0).getSavgAcctBeginBalance();

        log.info("last ending balance : {}", lastEndingBalance);
        log.info("created year month : {}", yearMonth);
        log.info("days in current month : {}", daysInMonth);
        log.info("acctStartDate : {}", acctStartDate);
        log.info("start Day : {}", startDay);
        log.info("startDate : {}", startDate);
        log.info("endDate : {}", endDate);

        for (int day = startDay; day <= daysInMonth; day++) {
            LocalDate currentDate = LocalDate.of(interestCalculationYear, interestCalculationMonth, day);
            List<PassbookResponseDTO> currentDatePassbookEntries = passbookResponseDTOList.stream()
                    .filter(passbook -> passbook.getTransactionDate().equals(currentDate))
                    .toList();
            log.info("total accrued interest before day : {} = {}", day, totalAccruedInterest);
            log.info("current date : {}", currentDate);
            log.info("current date passbook entries: {}", currentDatePassbookEntries);

            if (!currentDatePassbookEntries.isEmpty()) {
                log.info("passbook entry present");
                lastEndingBalance = currentDatePassbookEntries.get(currentDatePassbookEntries.size() - 1).getSavgAcctEndingBalance();
                log.info("last ending balance updated to : {}", lastEndingBalance);
                BigDecimal calculatedSavingsBalance = getSavingsBalanceMonoAccordingToCalculationMethod(currentDatePassbookEntries, data.getBalanceCalculationMethod(), data.getRoundingMode()).block();
                log.info("calculated savings balance : {}", calculatedSavingsBalance);
                if (calculatedSavingsBalance != null && validateMinimumBalanceRequiredInterestCalc(calculatedSavingsBalance, data.getBalanceRequiredInterestCalc())) {
                    log.info("calculated savings balance is not null & minimum balance validated");
                    totalAccruedInterest = getDailyAccruedInterest(data, totalAccruedInterest, currentDate, calculatedSavingsBalance);
                }
            } else if (validateMinimumBalanceRequiredInterestCalc(lastEndingBalance, data.getBalanceRequiredInterestCalc())) {
                log.info("No passbook entry exist. But last ending balance : {} validates minimum required balance.", lastEndingBalance);
                totalAccruedInterest = getDailyAccruedInterest(data, totalAccruedInterest, currentDate, lastEndingBalance);
            }
            daysCount++;
        }
        log.info("total accrued interest before returning : {}", totalAccruedInterest);

        return AccruedInterestDTODomain
                .builder()
                .savingsAccountId(data.getSavingsAccountId())
                .interestCalculationMonth(interestCalculationMonth)
                .interestCalculationYear(interestCalculationYear)
                .fromDate(startDate)
                .toDate(endDate)
                .accruedDays(daysCount)
                .accruedInterestAmount(totalAccruedInterest)
                .build();
    }




    public BigDecimal getSavingsAccountBalanceAtBeginningOfMonth(Integer interestCalculationMonth, Integer interestCalculationYear, CalculateInterestData data, Map<LocalDate, List<PassbookResponseDTO>> transactionDatePassbookListMap, Map<LocalDate, BigDecimal> transactionDateBalanceMap) {
        BigDecimal savingsBalanceAtBeginningOfMonth;
        List<LocalDate> transactionDatesSorted = transactionDateBalanceMap.keySet().stream().sorted().toList();
        LocalDate firstDateInMap = transactionDatesSorted.get(0);

        if (firstDateInMap == null) throw new RuntimeException("No transaction date found");
        YearMonth yearMonth = YearMonth.of(interestCalculationYear, interestCalculationMonth);

        log.info("transactionDateBalanceMap : {}", transactionDateBalanceMap);

        if (firstDateInMap.equals(data.getAcctStartDate()) && YearMonth.from(firstDateInMap).equals(yearMonth)) {
            savingsBalanceAtBeginningOfMonth = transactionDateBalanceMap.get(firstDateInMap);
            log.info("firstDateInMap : {}", firstDateInMap);
            log.info("savingsBalanceAtBeginningOfMonth : {}", savingsBalanceAtBeginningOfMonth);
        } else if (YearMonth.from(data.getAcctStartDate()).equals(yearMonth)
                && data.getAccountBalanceCalculationMethod()
                    .equalsIgnoreCase(AccountBalanceCalculationMethod.ACCOUNT_BALANCE_CALCULATION_METHOD_MONTHLY_OPEN_END_BASIS.getValue())) {
            savingsBalanceAtBeginningOfMonth = transactionDateBalanceMap.get(firstDateInMap);
        } else {
            savingsBalanceAtBeginningOfMonth = getSavingsBalanceAtBeginningOfMonth(transactionDatePassbookListMap, transactionDateBalanceMap);
            // when this month isn't account opening month &
            // the first transaction is not made in first day of month
            // we need to update the map so that interest is calculated from Day 1 of the calculating month

            if (firstDateInMap.getDayOfMonth() != 1
                    && (!YearMonth.from(firstDateInMap).equals(YearMonth.from(data.getAcctStartDate()))
                    || !YearMonth.from(firstDateInMap).equals(yearMonth))) {

                // when this month has no transaction history
                // the last passbook entry is from last month
                // we calculate & add the first date of calculating month in the map & delete the last month's entry
                // so that interest is calculated for this month only.

                if (!YearMonth.from(firstDateInMap).equals(yearMonth)) {
                    transactionDateBalanceMap.remove(firstDateInMap);
                    transactionDateBalanceMap.put(yearMonth.atDay(1), transactionDatePassbookListMap.get(firstDateInMap).get(0).getSavgAcctEndingBalance());
                    savingsBalanceAtBeginningOfMonth = transactionDatePassbookListMap
                            .get(firstDateInMap)
                            .get(0)
                            .getSavgAcctEndingBalance();
                } else transactionDateBalanceMap.put(
                        YearMonth.from(firstDateInMap).atDay(1),
                        savingsBalanceAtBeginningOfMonth);
                transactionDatesSorted = transactionDateBalanceMap.keySet().stream().sorted().toList();
                log.info("updated transactionDatesSorted : {}", transactionDatesSorted);
                log.info("updated map : {}", CommonFunctions.buildGsonBuilder(transactionDateBalanceMap).toString());
            }
        }

        return savingsBalanceAtBeginningOfMonth;
    }


    private BigDecimal getSavingsAccountBalanceAtEndingOfMonth(Map<LocalDate, BigDecimal> transactionDateBalanceMap) {
        List<LocalDate> transactionDatesSorted = transactionDateBalanceMap.keySet().stream().sorted().toList();
        return transactionDateBalanceMap.get(transactionDatesSorted.get(transactionDatesSorted.size()-1));
    }

    public Mono<AccruedInterestDTODomain> getMonthlyAccruedInterestAccordingToMonthlyOpeningEndingBalanceMethod(Integer interestCalculationMonth, Integer interestCalculationYear, List<PassbookResponseDTO> passbookResponseDTOList, CalculateInterestData data) {
        Map<LocalDate, List<PassbookResponseDTO>> transactionDatePassbookListMap = getTransactionDatePassbookListMap(passbookResponseDTOList);
        Map<LocalDate, BigDecimal> transactionDateBalanceMap = getTransactionDateBalanceMap(transactionDatePassbookListMap, data.getBalanceCalculationMethod(), data.getRoundingMode(), data.getBusinessDate());
        BigDecimal savingsAccountBalanceAtBeginningOfMonth = this.getSavingsAccountBalanceAtBeginningOfMonth(interestCalculationMonth, interestCalculationYear, data, transactionDatePassbookListMap, transactionDateBalanceMap);
        BigDecimal savingsAccountBalanceAtEndingOfMonth = this.getSavingsAccountBalanceAtEndingOfMonth(transactionDateBalanceMap);

        BigDecimal dailyAccountBalance = savingsAccountBalanceAtBeginningOfMonth.add(savingsAccountBalanceAtEndingOfMonth).divide(BigDecimal.valueOf(2), data.getRoundingMode());
        Optional<LocalDate> firstDate = transactionDateBalanceMap.keySet().stream().sorted().findFirst();
        LocalDate endDate = YearMonth.of(interestCalculationYear, interestCalculationMonth).atEndOfMonth();

        Long daysToAccrueInterest = firstDate.map(localDate -> ChronoUnit.DAYS.between(localDate, endDate) + 1).orElse(0L);
        BigDecimal provisionInterestRate = (data.getProvisionInterestRate() == null || data.getProvisionInterestRate().compareTo(BigDecimal.ZERO) == 0)
                                                ? data.getInterestRate()
                                                : data.getProvisionInterestRate();
        log.info("provision interest rate : {}", provisionInterestRate);
        provisionInterestRate = provisionInterestRate.divide(BigDecimal.valueOf(100), data.getInterestRatePrecision(), data.getRoundingMode());
        int daysInYear = data.getDaysInYear().equalsIgnoreCase("Actual")
                                ? data.getBusinessDate().isLeapYear()
                                    ? 366
                                    : 365
                                : 365;
        log.info("days in year : {}", daysInYear);

        BigDecimal dailyProvisionInterestRate = provisionInterestRate.divide(BigDecimal.valueOf(daysInYear), data.getInterestRatePrecision(), data.getRoundingMode());

        log.info("dailyAccountBalance : {}", dailyAccountBalance);
        BigDecimal accruedInterest = dailyAccountBalance.multiply(dailyProvisionInterestRate).multiply(BigDecimal.valueOf(daysToAccrueInterest));
        log.info("accrued Interest amount : {}", accruedInterest);
        accruedInterest = CommonFunctions.round(accruedInterest, data.getAccruedInterestPrecision(), data.getRoundingMode());

        AccruedInterestDTODomain accruedInterestDomain = AccruedInterestDTODomain
                .builder()
                .savingsAccountId(data.getSavingsAccountId())
                .interestCalculationMonth(interestCalculationMonth)
                .interestCalculationYear(interestCalculationYear)
                .fromDate(firstDate.get())
                .toDate(endDate)
                .accruedDays(daysToAccrueInterest.intValue())
                .accruedInterestAmount(accruedInterest)
                .savgAcctBeginBalance(savingsAccountBalanceAtBeginningOfMonth)
                .savgAcctEndingBalance(savingsAccountBalanceAtEndingOfMonth)
                .build();

        log.info("accrued interest domain : {}", accruedInterestDomain);

        /*return accruedInterestPort
                .saveAccruedInterestV2(SavingsAccountInterestDeposit
                        .builder()
                        .accruedInterestId(UUID.randomUUID().toString())
                        .savingsAccountId(accruedInterestDomain.getSavingsAccountId())
                        .savingsAccountOid(data.getSavingsAccountOid())
                        .managementProcessId(data.getManagementProcessId())
                        .processId(data.getProcessId())
                        .officeId(data.getOfficeId())
                        .samityId(data.getSamityId())
                        .memberId(data.getMemberId())
                        .productId(data.getSavingsProductId())
                        .savingsTypeId(data.getSavingsTypeId())
                        .interestCalculationMonth(interestCalculationMonth)
                        .interestCalculationYear(interestCalculationYear)
                        .accruedInterestAmount(accruedInterest)
                        .savgAcctBeginBalance(savingsAccountBalanceAtBeginningOfMonth)
                        .savgAcctEndingBalance(savingsAccountBalanceAtEndingOfMonth)
                        .fromDate(firstDate.get())
                        .toDate(endDate)
                        .status(Status.STATUS_PENDING.getValue())
                        .createdOn(LocalDateTime.now())
                        .createdBy(data.getLoginId())
                        .build())
                .thenReturn(accruedInterestDomain);*/
        return Mono.just(accruedInterestDomain);
    }



    public Mono<AccruedInterestDTODomain> getMonthlyAccruedInterestForGsVs(Integer interestCalculationMonth, Integer interestCalculationYear, List<PassbookResponseDTO> passbookResponseDTOList, CalculateInterestData data) {
        Map<LocalDate, List<PassbookResponseDTO>> transactionDatePassbookListMap = getTransactionDatePassbookListMap(passbookResponseDTOList);
        Map<LocalDate, BigDecimal> transactionDateBalanceMap = getTransactionDateBalanceMap(transactionDatePassbookListMap, data.getBalanceCalculationMethod(), data.getRoundingMode(), data.getBusinessDate());
        YearMonth yearMonth = YearMonth.of(interestCalculationYear, interestCalculationMonth);
        BigDecimal interestRatePerDay = calculateInterestRatePerDay(data);
        List<LocalDate> transactionDatesSorted = transactionDateBalanceMap.keySet().stream().sorted().toList();
        log.info("transactionDates : {}", transactionDatesSorted);
        BigDecimal savingsBalanceAtBeginningOfMonth;

        savingsBalanceAtBeginningOfMonth = this.getSavingsAccountBalanceAtBeginningOfMonth(interestCalculationMonth, interestCalculationYear, data, transactionDatePassbookListMap, transactionDateBalanceMap);

        log.info("Map : {}", transactionDateBalanceMap);
        log.info("Pretty Map : {}", new GsonBuilder().setPrettyPrinting().create().toJson(transactionDateBalanceMap));
        log.info("savingsBalanceAtBeginningOfMonth : {}", savingsBalanceAtBeginningOfMonth);
        BigDecimal totalInterest = BigDecimal.ZERO;
        int lastElementIndex = transactionDatesSorted.size() - 1;

        long totalNumberOfDaysAccrued = 0;
        for (int i = 0; i<transactionDatesSorted.size(); i++) {
            BigDecimal presentSavingsBalance = (i == 0) ? savingsBalanceAtBeginningOfMonth : transactionDateBalanceMap.get(transactionDatesSorted.get(i));
            long noOfDaysInBetweenTransactions = (i == lastElementIndex)
                ? getNumberOfDaysForLastTransactionDate(transactionDatesSorted.get(lastElementIndex), yearMonth)
                : getNumberOfDaysInBetween(transactionDatesSorted.get(i), transactionDatesSorted.get(i+1), yearMonth.lengthOfMonth());

            totalNumberOfDaysAccrued += noOfDaysInBetweenTransactions;
            log.info("totalNumberOfDaysAccrued : {}", totalNumberOfDaysAccrued);
            log.info("interestRatePerDay : {}", interestRatePerDay);
            BigDecimal interestPerDayForThisPeriod = presentSavingsBalance.compareTo(data.getBalanceRequiredInterestCalc()) >= 0 && (transactionDatesSorted.get(i).isAfter(data.getAcctStartDate()) || transactionDatesSorted.get(i).isEqual(data.getAcctStartDate()))
                                                ? presentSavingsBalance.multiply(interestRatePerDay)
                                                : BigDecimal.ZERO;
            log.info("interestPerDay unrounded : {}", interestPerDayForThisPeriod);
            BigDecimal interestPerDayRounded = CommonFunctions.round(interestPerDayForThisPeriod, data.getAccruedInterestPrecision(), data.getRoundingMode());
            log.info("interestPerDay rounded : {}", interestPerDayRounded);
            BigDecimal interestForThisPeriod = interestPerDayRounded.multiply(BigDecimal.valueOf(noOfDaysInBetweenTransactions));
            interestForThisPeriod = CommonFunctions.round(interestForThisPeriod, data.getAccruedInterestPrecision(), data.getRoundingMode());
            log.info("interestForThisPeriod : i = {} | transactionDate : {} | present savings balance : {} | interest amount : {}", i, transactionDatesSorted.get(i), presentSavingsBalance, interestForThisPeriod);

            totalInterest = totalInterest.add(interestForThisPeriod);
            log.info("total interest now : {}", totalInterest);
        }

        Optional<LocalDate> firstDateFromMap = transactionDateBalanceMap.keySet().stream().sorted().findFirst();
        BigDecimal savgAcctEndingBalance = transactionDateBalanceMap.get(transactionDatesSorted.get(transactionDatesSorted.size()-1));
        List<PassbookResponseDTO> listToBeSorted = new ArrayList<>(passbookResponseDTOList);
        listToBeSorted.sort(Comparator.comparing(PassbookResponseDTO::getTransactionDate));
        /*BigDecimal savgAcctBeginBalance = listToBeSorted.get(0).getSavgAcctBeginBalance();
        if (!YearMonth.from(passbookResponseDTOList.get(passbookResponseDTOList.size()-1).getTransactionDate()).equals(yearMonth)) {
           savgAcctBeginBalance = listToBeSorted.get(0).getSavgAcctEndingBalance();
        }*/

        log.info("listToBeSorted : {}", listToBeSorted);

        BigDecimal savgAcctBeginBalance = YearMonth.from(passbookResponseDTOList.get(passbookResponseDTOList.size()-1).getTransactionDate()).equals(yearMonth)
                ? listToBeSorted.get(0).getSavgAcctBeginBalance()
                : listToBeSorted.get(0).getSavgAcctEndingBalance();

        log.info("savgAcctBeginBalance : {}", savgAcctBeginBalance);

        System.out.println("totalNumberOfDaysAccrued : " + totalNumberOfDaysAccrued);
        AccruedInterestDTODomain accruedInterestDomain = AccruedInterestDTODomain
                .builder()
                .savingsAccountId(data.getSavingsAccountId())
                .interestCalculationMonth(interestCalculationMonth)
                .interestCalculationYear(interestCalculationYear)
                .fromDate(firstDateFromMap.get())
                .toDate(yearMonth.atEndOfMonth())
                .accruedDays((int) totalNumberOfDaysAccrued)
                .accruedInterestAmount(totalInterest)
                .savgAcctBeginBalance(savgAcctBeginBalance)
                .savgAcctEndingBalance(savgAcctEndingBalance)
                .build();

        /*return accruedInterestPort
                .saveAccruedInterestV2(SavingsAccountInterestDeposit
                        .builder()
                        .accruedInterestId(UUID.randomUUID().toString())
                        .savingsAccountId(accruedInterestDomain.getSavingsAccountId())
                        .savingsAccountOid(data.getSavingsAccountOid())
                        .managementProcessId(data.getManagementProcessId())
                        .processId(data.getProcessId())
                        .officeId(data.getOfficeId())
                        .samityId(data.getSamityId())
                        .memberId(data.getMemberId())
                        .productId(data.getSavingsProductId())
                        .savingsTypeId(data.getSavingsTypeId())
                        .interestCalculationMonth(interestCalculationMonth)
                        .interestCalculationYear(interestCalculationYear)
                        .accruedInterestAmount(totalInterest)
                        .savgAcctBeginBalance(savgAcctBeginBalance)
                        .savgAcctEndingBalance(savgAcctEndingBalance)
                        .fromDate(firstDateFromMap.get())
                        .toDate(yearMonth.atEndOfMonth())
                        .status(Status.STATUS_PENDING.getValue())
                        .createdOn(LocalDateTime.now())
                        .createdBy(data.getLoginId())
                        .build())
                .thenReturn(accruedInterestDomain);*/
        return Mono.just(accruedInterestDomain);

    }

    Mono<AccruedInterestDTODomain>  getMonthlyAccruedInterestForDPS(Integer interestCalculationMonth, Integer interestCalculationYear, List<PassbookResponseDTO> passbookResponseDTOList, CalculateInterestData data) {
        Map<LocalDate, List<PassbookResponseDTO>> transactionDatePassbookListMap = getTransactionDatePassbookListMap(passbookResponseDTOList);
        Map<LocalDate, BigDecimal> transactionDateBalanceMap = getTransactionDateBalanceMap(transactionDatePassbookListMap, data.getBalanceCalculationMethod(), data.getRoundingMode(), data.getBusinessDate());
        LocalDate firstDateInMap = transactionDateBalanceMap.keySet().stream().sorted().findFirst().orElseThrow(() -> new RuntimeException("No transaction date found"));
        YearMonth yearMonth = YearMonth.of(interestCalculationYear, interestCalculationMonth);
        BigDecimal interestRatePerDay = calculateInterestRatePerDay(data);
        List<LocalDate> transactionDatesSorted = transactionDateBalanceMap.keySet().stream().sorted().toList();
        log.info("transactionDates : {}", transactionDatesSorted);
        BigDecimal savingsBalanceAtBeginningOfMonth;
        assert firstDateInMap != null;

        if (firstDateInMap.equals(data.getAcctStartDate())) {
            savingsBalanceAtBeginningOfMonth = transactionDateBalanceMap.get(firstDateInMap);
            log.info("firstDateInMap : {}", firstDateInMap);
            log.info("savingsBalanceAtBeginningOfMonth : {}", savingsBalanceAtBeginningOfMonth);
        } else {
            savingsBalanceAtBeginningOfMonth = getSavingsBalanceAtBeginningOfMonth(transactionDatePassbookListMap, transactionDateBalanceMap);
            // when this month isn't account opening month &
            // the first transaction is not made in first day of month
            // we need to update the map so that interest is calculated from Day 1 of the calculating month

            if (transactionDatesSorted.get(0).getDayOfMonth() != 1
                    && (!YearMonth.from(transactionDatesSorted.get(0)).equals(YearMonth.from(data.getAcctStartDate()))
                    || !YearMonth.from(transactionDatesSorted.get(0)).equals(yearMonth))) {

                // when this month has no transaction history
                // the last passbook entry is from last month
                // we calculate & add the first date of calculating month in the map & delete the last month's entry
                // so that interest is calculated for this month only.

                if (!YearMonth.from(transactionDatesSorted.get(0)).equals(yearMonth)) {
                    transactionDateBalanceMap.remove(transactionDatesSorted.get(0));
                    transactionDateBalanceMap.put(yearMonth.atDay(1), transactionDatePassbookListMap.get(transactionDatesSorted.get(0)).get(0).getSavgAcctEndingBalance());
                    savingsBalanceAtBeginningOfMonth = transactionDatePassbookListMap
                            .get(transactionDatesSorted.get(0))
                            .get(0)
                            .getSavgAcctEndingBalance();
                } else transactionDateBalanceMap.put(
                        YearMonth.from(firstDateInMap).atDay(1),
                        savingsBalanceAtBeginningOfMonth);
                transactionDatesSorted = transactionDateBalanceMap.keySet().stream().sorted().toList();
                log.info("updated transactionDatesSorted : {}", transactionDatesSorted);
                log.info("updated map : {}", CommonFunctions.buildGsonBuilder(transactionDateBalanceMap).toString());
            }
        }

        log.info("Map : {}", transactionDateBalanceMap);
        log.info("Pretty Map : {}", new GsonBuilder().setPrettyPrinting().create().toJson(transactionDateBalanceMap));
        log.info("savingsBalanceAtBeginningOfMonth : {}", savingsBalanceAtBeginningOfMonth);
        BigDecimal totalInterest = BigDecimal.ZERO;
        int lastElementIndex = transactionDatesSorted.size() - 1;

        long totalNumberOfDaysAccrued = 0;
        AtomicInteger accrualCount = new AtomicInteger();
        BigDecimal compoundBalance;
        for (int i = 0; i<transactionDatesSorted.size(); i++) {
            /*interestCompoundPort
                    .getInterestCompoundBySavingsAccountId(data.getSavingsAccountId())
                    .switchIfEmpty(passbookUseCase.getPassbookEntriesByTransactionCodeAndSavingsAccountId(TransactionCodes.SAVINGS_DEPOSIT.getValue(), data.getSavingsAccountId())
                            .map(passbookResponseDTOS -> {
                                accrualCount.set(passbookResponseDTOS.size());
                                return InterestCompoundDTO.builder().build();
                            }))
                    .filter(interestCompoundDTO -> interestCompoundDTO.getSavingsAccountId() != null)
                    .map(interestCompoundDTO -> )*/

            BigDecimal presentSavingsBalance = (i == 0) ? savingsBalanceAtBeginningOfMonth : transactionDateBalanceMap.get(transactionDatesSorted.get(i));
            long noOfDaysInBetweenTransactions = (i == lastElementIndex)
                    ? getNumberOfDaysForLastTransactionDate(transactionDatesSorted.get(lastElementIndex), yearMonth)
                    : getNumberOfDaysInBetween(transactionDatesSorted.get(i), transactionDatesSorted.get(i+1), yearMonth.lengthOfMonth());

            totalNumberOfDaysAccrued += noOfDaysInBetweenTransactions;
            log.info("totalNumberOfDaysAccrued : {}", totalNumberOfDaysAccrued);
            log.info("interestRatePerDay : {}", interestRatePerDay);
            BigDecimal interestPerDayForThisPeriod = presentSavingsBalance.compareTo(data.getBalanceRequiredInterestCalc()) >= 0 && (transactionDatesSorted.get(i).isAfter(data.getAcctStartDate()) || transactionDatesSorted.get(i).isEqual(data.getAcctStartDate()))
                    ? presentSavingsBalance.multiply(interestRatePerDay)
                    : BigDecimal.ZERO;
            log.info("interestPerDay unrounded : {}", interestPerDayForThisPeriod);
            BigDecimal interestPerDayRounded = CommonFunctions.round(interestPerDayForThisPeriod, data.getAccruedInterestPrecision(), data.getRoundingMode());
            log.info("interestPerDay rounded : {}", interestPerDayRounded);
            BigDecimal interestForThisPeriod = interestPerDayRounded.multiply(BigDecimal.valueOf(noOfDaysInBetweenTransactions));
            interestForThisPeriod = CommonFunctions.round(interestForThisPeriod, data.getAccruedInterestPrecision(), data.getRoundingMode());
            log.info("interestForThisPeriod : i = {} | transactionDate : {} | present savings balance : {} | interest amount : {}", i, transactionDatesSorted.get(i), presentSavingsBalance, interestForThisPeriod);

            totalInterest = totalInterest.add(interestForThisPeriod);
            log.info("total interest now : {}", totalInterest);
        }

        Optional<LocalDate> firstDateFromMap = transactionDateBalanceMap.keySet().stream().sorted().findFirst();
        BigDecimal savgAcctEndingBalance = transactionDateBalanceMap.get(transactionDatesSorted.get(transactionDatesSorted.size()-1));
        List<PassbookResponseDTO> listToBeSorted = new ArrayList<>(passbookResponseDTOList);
        listToBeSorted.sort(Comparator.comparing(PassbookResponseDTO::getTransactionDate));
        /*BigDecimal savgAcctBeginBalance = listToBeSorted.get(0).getSavgAcctBeginBalance();
        if (!YearMonth.from(passbookResponseDTOList.get(passbookResponseDTOList.size()-1).getTransactionDate()).equals(yearMonth)) {
           savgAcctBeginBalance = listToBeSorted.get(0).getSavgAcctEndingBalance();
        }*/

        log.info("listToBeSorted : {}", listToBeSorted);

        BigDecimal savgAcctBeginBalance = YearMonth.from(passbookResponseDTOList.get(passbookResponseDTOList.size()-1).getTransactionDate()).equals(yearMonth)
                ? listToBeSorted.get(0).getSavgAcctBeginBalance()
                : listToBeSorted.get(0).getSavgAcctEndingBalance();

        log.info("savgAcctBeginBalance : {}", savgAcctBeginBalance);

        System.out.println("totalNumberOfDaysAccrued : " + totalNumberOfDaysAccrued);
        AccruedInterestDTODomain accruedInterestDomain = AccruedInterestDTODomain
                .builder()
                .savingsAccountId(data.getSavingsAccountId())
                .interestCalculationMonth(interestCalculationMonth)
                .interestCalculationYear(interestCalculationYear)
                .fromDate(firstDateFromMap.get())
                .toDate(yearMonth.atEndOfMonth())
                .accruedDays((int) totalNumberOfDaysAccrued)
                .accruedInterestAmount(totalInterest)
                .savgAcctBeginBalance(savgAcctBeginBalance)
                .savgAcctEndingBalance(savgAcctEndingBalance)
                .build();
        return Mono.just(accruedInterestDomain);

        /*return accruedInterestPort
                .saveAccruedInterestV2(SavingsAccountInterestDeposit
                        .builder()
                        .accruedInterestId(UUID.randomUUID().toString())
                        .savingsAccountId(accruedInterestDomain.getSavingsAccountId())
                        .savingsAccountOid(data.getSavingsAccountOid())
                        .managementProcessId(data.getManagementProcessId())
                        .processId(data.getProcessId())
                        .officeId(data.getOfficeId())
                        .samityId(data.getSamityId())
                        .productId(data.getSavingsProductId())
                        .savingsTypeId(data.getSavingsTypeId())
                        .interestCalculationMonth(interestCalculationMonth)
                        .interestCalculationYear(interestCalculationYear)
                        .accruedInterestAmount(totalInterest)
                        .savgAcctBeginBalance(savgAcctBeginBalance)
                        .savgAcctEndingBalance(savgAcctEndingBalance)
                        .fromDate(firstDateFromMap.get())
                        .toDate(yearMonth.atEndOfMonth())
                        .status(Status.STATUS_PENDING.getValue())
                        .createdOn(LocalDateTime.now())
                        .createdBy(data.getLoginId())
                        .build())
                .thenReturn(accruedInterestDomain);*/
    }






    private BigDecimal getSavingsBalanceAtBeginningOfMonth(Map<LocalDate, List<PassbookResponseDTO>> transactionDatePassbookListMap, Map<LocalDate, BigDecimal> transactionDateBalanceMap) {
        List<LocalDate> transactionDates = new ArrayList<>();
        transactionDates = transactionDatePassbookListMap.keySet().stream().sorted().toList();
        LocalDate firstEntryInDateList = transactionDates.get(0);
        List<PassbookResponseDTO> passbookListForFirstDate = transactionDatePassbookListMap.get(firstEntryInDateList);
        log.info("passbookListSizeForFirstDate : {}", passbookListForFirstDate.size());
        BigDecimal savingsBalanceAtBeginningOfMonth = passbookListForFirstDate.size() > 1 && firstEntryInDateList.getDayOfMonth() == 1
                                                            ? transactionDateBalanceMap.get(firstEntryInDateList)
                                                            : passbookListForFirstDate.get(0).getSavgAcctBeginBalance();
        log.info("savingsBalanceAtBeginningOfMonth : {}", savingsBalanceAtBeginningOfMonth);

        return savingsBalanceAtBeginningOfMonth;

    }

    public Long getNumberOfDaysInBetween(LocalDate currentDate, LocalDate nextDate, Integer daysInCurrentMonth) {
        long numberOfDaysInBetween = Math.abs(ChronoUnit.DAYS.between(currentDate, nextDate));

        if (numberOfDaysInBetween == 0)
            numberOfDaysInBetween = 1L;

        log.info("daysInCurrentMonth : {}", daysInCurrentMonth);
        log.info("currentDate : {}", currentDate);
        log.info("nextDate : {}", nextDate);
        log.info("numberOfDaysInBetween : {}", numberOfDaysInBetween);

        return numberOfDaysInBetween;
    }

    private BigDecimal calculateInterestRatePerDay(CalculateInterestData data) {
        int daysInYear = data.getDaysInYear().equalsIgnoreCase("Actual")
                ? data.getBusinessDate().isLeapYear()
                    ? 366
                    : 365
                : 365;
        return data.getInterestRate()
                .divide(BigDecimal.valueOf(100L * daysInYear), data.getInterestRatePrecision(), data.getRoundingMode());
    }

    private Long getNumberOfDaysForLastTransactionDate(LocalDate date, YearMonth yearMonth) {
        /*
         check if it is the last date of month
            yes -> number of days between = 1
            no -> get number of days between & add 1
        */
        long numberOfDaysInBetween = (date.equals(yearMonth.atEndOfMonth()))
                ? 1
                : getNumberOfDaysInBetween(date, yearMonth.atEndOfMonth(), yearMonth.lengthOfMonth()) + 1;
        log.info("date : {}", date);
//        log.info("last date of month : {}", yearMonth.atEndOfMonth());
        log.info("numberOfDaysInBetween : {}", numberOfDaysInBetween);
        return numberOfDaysInBetween;
    }

    private Map<LocalDate, BigDecimal> getTransactionDateBalanceMap(Map<LocalDate, List<PassbookResponseDTO>> transactionDatePassbookListMap, String balanceCalculationMethod, RoundingMode roundingMode, LocalDate businessDate) {
        Map<LocalDate, BigDecimal> dateBalanceMap = new HashMap<>();

        for (LocalDate transactionDate : transactionDatePassbookListMap.keySet()) {
            List<PassbookResponseDTO> passbookListInADate = transactionDatePassbookListMap.get(transactionDate);
            BigDecimal savingsBalance = getSavingsBalanceAccordingToCalculationMethod(passbookListInADate, balanceCalculationMethod, roundingMode);
            dateBalanceMap.put(transactionDate, savingsBalance);

//            handleMultipleTransactions(dateBalanceMap, transactionDate, passbookListInADate);
        }
        log.info("businessDate : {}", businessDate);
        log.info("dateBalanceMap before putting businessDate : {}", dateBalanceMap);

        dateBalanceMap.put(businessDate, dateBalanceMap.get(dateBalanceMap.keySet().stream().sorted().toList().get(dateBalanceMap.size()-1)));
        log.info("dateBalanceMap after putting businessDate : {}", dateBalanceMap);
        return dateBalanceMap;
    }

    private void handleMultipleTransactions(Map<LocalDate, BigDecimal> dateBalanceMap, LocalDate transactionDate, List<PassbookResponseDTO> passbookListInADate) {
        if (passbookListInADate.size() > 1) {
            LocalDate nextDate = transactionDate.plusDays(1);
            passbookListInADate.sort(Comparator.comparing(PassbookResponseDTO::getTransactionDate));
            dateBalanceMap.put(nextDate, passbookListInADate.get(passbookListInADate.size() - 1).getSavgAcctEndingBalance());
        }
    }

    private Map<LocalDate, List<PassbookResponseDTO>> getTransactionDatePassbookListMap(List<PassbookResponseDTO> passbookList) {
        Map<LocalDate, List<PassbookResponseDTO>> datePassbookListMap = new HashMap<>();

        for (PassbookResponseDTO passbook : passbookList) {
            if (datePassbookListMap.containsKey(passbook.getTransactionDate())) {
                List<PassbookResponseDTO> passbookResponseDTOS = datePassbookListMap.get(passbook.getTransactionDate());
                passbookResponseDTOS.add(passbook);
            } else {
                List<PassbookResponseDTO> passbookResponseDTOList = new ArrayList<>();
                passbookResponseDTOList.add(passbook);
                datePassbookListMap.put(passbook.getTransactionDate(), passbookResponseDTOList);
            }
        }
        return datePassbookListMap;
    }


    private BigDecimal getDailyAccruedInterest(CalculateInterestData data, BigDecimal totalAccruedInterest, LocalDate currentDate, BigDecimal calculatedSavingsBalance) {
        BigDecimal dailyAccruedInterest = savingsInterestCommands.calculateDailyAccruedInterest(calculatedSavingsBalance, data.getInterestRate(), data.getInterestRateFrequency(), data.getInterestRatePrecision(), data.getAccruedInterestPrecision(), data.getRoundingMode(), data.getDaysInYear(), currentDate);

        log.info("calculated daily accrued interest : {}", dailyAccruedInterest);
        log.info("total accrued interest before : {}", totalAccruedInterest);
        totalAccruedInterest = totalAccruedInterest.add(dailyAccruedInterest);
        log.info("total accrued interest after : {}", totalAccruedInterest);
        return totalAccruedInterest;
    }

    private SavingsAccruedInterestResponseDTO buildResponseForMonthlyAccruedInterest(AccruedInterestDTODomain accruedInterestDTODomain, CalculateInterestData data, CalculateInterestCommand command) {
        return SavingsAccruedInterestResponseDTO
                .builder()
                .memberId(data.getMemberId())
                .mfiId(data.getMfiId())
                .savingsAccountId(command.getSavingsAccountId())
                .savingsAccountOid(data.getSavingsAccountOid())
                .interestCalculationMonth(accruedInterestDTODomain.getInterestCalculationMonth())
                .interestCalculationYear(accruedInterestDTODomain.getInterestCalculationYear())
                .accruedInterestAmount(accruedInterestDTODomain.getAccruedInterestAmount())
                .fromDate(accruedInterestDTODomain.getFromDate())
                .toDate(accruedInterestDTODomain.getToDate())
                .accruedDays(accruedInterestDTODomain.getAccruedDays())
                .savingsProductId(data.getSavingsProductId())
                .build();
    }

    private boolean validateMinimumBalanceRequiredInterestCalc(BigDecimal savingsBalance, BigDecimal minimumBalanceRequired) {
        return savingsBalance.doubleValue() >= minimumBalanceRequired.doubleValue();
    }

    public Mono<BigDecimal> getSavingsBalanceMonoAccordingToCalculationMethod(List<PassbookResponseDTO> passbookList, String balanceCalculationMethod, RoundingMode roundingMode) {
        log.info("Balance calculation method : {}", balanceCalculationMethod);

        if (balanceCalculationMethod.equalsIgnoreCase(SavingsBalanceCalculationMethods.AVERAGE_DAILY_BALANCE.getValue())) {
            return getAverageDailyBalanceMono(passbookList, roundingMode);
        } else if (balanceCalculationMethod.equalsIgnoreCase(SavingsBalanceCalculationMethods.MINIMUM_DAILY_BALANCE.getValue())) {
            return getMinimumDailyBalanceMono(passbookList);
        } else if (balanceCalculationMethod.equalsIgnoreCase(SavingsBalanceCalculationMethods.END_OF_DAY_BALANCE.getValue())) {
            return getDayEndBalanceMono(passbookList);
        } else {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Invalid Balance Calculation Method!"));
        }
    }

    private Mono<BigDecimal> getAverageDailyBalanceMono(List<PassbookResponseDTO> passbookList, RoundingMode roundingMode) {

        BigDecimal totalSavgAcctEndingBalance = BigDecimal.ZERO;
        BigDecimal averageDailyBalance;

        for(PassbookResponseDTO passbookResponseDTO : passbookList) {
            totalSavgAcctEndingBalance = totalSavgAcctEndingBalance.add(passbookResponseDTO.getSavgAcctEndingBalance());
        }

        log.info("totalSavgAcctEndingBalance : " + totalSavgAcctEndingBalance);
        log.info("number of transaction : " + passbookList.size());
        averageDailyBalance = totalSavgAcctEndingBalance.divide(BigDecimal.valueOf(passbookList.size()), roundingMode);
        log.info("calculated average daily balance : {}", averageDailyBalance);
        return Mono.just(averageDailyBalance);

    }

    private Mono<BigDecimal> getMinimumDailyBalanceMono(List<PassbookResponseDTO> passbookList) {

        BigDecimal minBalance = passbookList.get(0).getSavgAcctEndingBalance();
        for (PassbookResponseDTO passbookResponseDTO : passbookList) {
            BigDecimal balance = passbookResponseDTO.getSavgAcctEndingBalance();
            if (balance.compareTo(minBalance) < 0) {
                minBalance = balance;
            }
        }

        log.info("Minimum daily balance: {}", minBalance);

        return Mono.just(minBalance);
    }

    private Mono<BigDecimal> getDayEndBalanceMono(List<PassbookResponseDTO> passbookList) {
        log.info("getDayEndBalance : {}", passbookList);
        return Mono.just(passbookList.get(passbookList.size() - 1).getSavgAcctEndingBalance());
    }

    public BigDecimal getSavingsBalanceAccordingToCalculationMethod(List<PassbookResponseDTO> passbookList, String balanceCalculationMethod, RoundingMode roundingMode) {
        log.info("Balance calculation method : {}", balanceCalculationMethod);

        if (balanceCalculationMethod.equalsIgnoreCase(SavingsBalanceCalculationMethods.AVERAGE_DAILY_BALANCE.getValue())) {
            return getAverageDailyBalance(passbookList, roundingMode);
        } else if (balanceCalculationMethod.equalsIgnoreCase(SavingsBalanceCalculationMethods.MINIMUM_DAILY_BALANCE.getValue())) {
            return getMinimumDailyBalance(passbookList);
        } else if (balanceCalculationMethod.equalsIgnoreCase(SavingsBalanceCalculationMethods.END_OF_DAY_BALANCE.getValue())) {
            return getDayEndBalance(passbookList);
        } else {
            throw new IllegalArgumentException("Invalid Balance Calculation Method!");
        }
    }

    private BigDecimal getAverageDailyBalance(List<PassbookResponseDTO> passbookList, RoundingMode roundingMode) {

        BigDecimal totalSavgAcctEndingBalance = BigDecimal.ZERO;
        BigDecimal averageDailyBalance;

        for(PassbookResponseDTO passbookResponseDTO : passbookList) {
            totalSavgAcctEndingBalance = totalSavgAcctEndingBalance.add(passbookResponseDTO.getSavgAcctEndingBalance());
        }

        log.info("totalSavgAcctEndingBalance : " + totalSavgAcctEndingBalance);
        log.info("number of transaction : " + passbookList.size());
        averageDailyBalance = totalSavgAcctEndingBalance.divide(BigDecimal.valueOf(passbookList.size()), roundingMode);
        log.info("calculated average daily balance : {}", averageDailyBalance);
        return averageDailyBalance;

    }

    private BigDecimal getMinimumDailyBalance(List<PassbookResponseDTO> passbookList) {

        BigDecimal minBalance = passbookList.get(0).getSavgAcctEndingBalance();
        for (PassbookResponseDTO passbookResponseDTO : passbookList) {
            BigDecimal balance = passbookResponseDTO.getSavgAcctEndingBalance();
            if (balance.compareTo(minBalance) < 0) {
                minBalance = balance;
            }
        }

        log.info("Minimum daily balance: {}", minBalance);

        return minBalance;
    }

    private BigDecimal getDayEndBalance(List<PassbookResponseDTO> passbookList) {
        log.info("getDayEndBalance : {}", passbookList);
        return passbookList.get(passbookList.size() - 1).getSavgAcctEndingBalance();
    }

    SavingsInterestResponseDTO buildResponse(Tuple2<BigDecimal, BigDecimal> tupleOfAvailableBalanceAndAccruedInterest, CalculateInterestCommand command) {
        return SavingsInterestResponseDTO
                .builder()
                .savingsAccountId(command.getSavingsAccountId())
                .availableSavings(tupleOfAvailableBalanceAndAccruedInterest.getT1())
                .accruedInterest(tupleOfAvailableBalanceAndAccruedInterest.getT2())
                .interestCalculationDate(command.getInterestCalculationDate())
                .build();
    }

    private Mono<CalculateInterestData> buildData(Tuple2<SavingsAccountProductEntity, MetaPropertyResponseDTO> tuple2, CalculateInterestCommand command) {
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
                    .daysInYear(DaysInYear._365.getValue())
                    .roundingLogic(RoundingMode.HALF_UP.toString())
                    .build();

        LocalDate accountOpeningDate = savingsAccountProductEntity.getAcctStartDate();

        if (command.getInterestCalculationYear() != null && command.getInterestCalculationMonth() != null ) {
            LocalDate firstDayOfInterestCalculationMonth = LocalDate.of(command.getInterestCalculationYear(), command.getInterestCalculationMonth(), 1);
            if (YearMonth.from(firstDayOfInterestCalculationMonth).isBefore(YearMonth.from(accountOpeningDate)))
                return Mono.just(CalculateInterestData
                        .builder()
                        .savingsAccountOid(null)
                        .build());
        }

        /*if (YearMonth.from(firstDayOfInterestCalculationMonth).isBefore(YearMonth.from(accountOpeningDate)))
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.INTEREST_CANNOT_BE_CALCULATED.getValue()));*/



        return Mono.just(CalculateInterestData
                .builder()
                .acctStartDate(savingsAccountProductEntity.getAcctStartDate())
                .interestRateFrequency(savingsAccountProductEntity.getInterestRateFrequency())
                .interestRatePrecision(metaProperty.getInterestRatePrecision())
                .accruedInterestPrecision(metaProperty.getAccruedInterestPrecision())
                .roundingMode(getRoundingMode(metaProperty.getRoundingLogic()))
                .daysInYear(metaProperty.getDaysInYear())
                .balanceCalculationMethod(savingsAccountProductEntity.getInterestCalculatedUsing())
                .interestPostingPeriod(savingsAccountProductEntity.getInterestPostingPeriod())
                .interestCompoundingPeriod(savingsAccountProductEntity.getInterestCompoundingPeriod())
                .savingsAccountId(command.getSavingsAccountId())
                .interestRate(savingsAccountProductEntity.getInterestRate())
                .interestCalculationDate(command.getInterestCalculationDate())
                .interestCalculationMonth(command.getInterestCalculationMonth())
                .interestCalculationYear(command.getInterestCalculationYear())
                .balanceRequiredInterestCalc(BigDecimal.valueOf(Double.parseDouble(savingsAccountProductEntity.getBalanceRequiredInterestCalc())))
                .mfiId(savingsAccountProductEntity.getMfiId())
                .memberId(savingsAccountProductEntity.getMemberId())
                .savingsAccountOid(savingsAccountProductEntity.getSavingsAccountOid())
                .savingsProductId(savingsAccountProductEntity.getSavingsProductId())
                .savingsTypeId(savingsAccountProductEntity.getSavingsTypeId())
                .businessDate(command.getBusinessDate())
                .build());
    }

    private RoundingMode getRoundingMode(String roundingLogic) {
        RoundingMode roundingMode = null;
        switch (roundingLogic.toUpperCase()) {
            case "HALFUP" -> roundingMode = RoundingMode.HALF_UP;
            case "HALFDOWN" -> roundingMode = RoundingMode.HALF_DOWN;
            case "UP" -> roundingMode = RoundingMode.UP;
            case "DOWN" -> roundingMode = RoundingMode.DOWN;
        }
        return roundingMode;
    }

}
