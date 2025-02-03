package net.celloscope.mraims.loanportfolio.features.fdr.application.service;

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

import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto.DPSClosureCommand;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto.DPSClosureDTO;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosure;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.*;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.out.FDRClosurePersistencePort;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.*;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response.AccruedInterestResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.DPSAccountDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.FDRAccountDTO;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.CalendarUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.HolidayUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.FDRInterestCalculationEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.FDRUseCase;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.out.FDRPersistencePort;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.CalculationMetaProperty;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.AccruedInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.AccruedInterestCommand;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.helpers.dto.response.SingleTransactionResponseDTO;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Constants.*;

@Service
@Slf4j
public class FDRService implements FDRUseCase {
        private final CommonRepository commonRepository;
        private final HolidayUseCase holidayUseCase;
        private final MetaPropertyUseCase metaPropertyUseCase;
        private final FDRPersistencePort port;
        private final TransactionUseCase transactionUseCase;
        private final PassbookUseCase passbookUseCase;
        private final TransactionalOperator rxtx;
        private final ISavingsAccountUseCase savingsAccountUseCase;
        private final CalendarUseCase calendarUseCase;
        private final AccruedInterestUseCase accruedInterestUseCase;
        private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
        private final FDRClosurePersistencePort fdrClosurePersistencePort;
        private final ModelMapper modelMapper;

        public FDRService(TransactionalOperator rxtx, CommonRepository commonRepository, HolidayUseCase holidayUseCase,
                          MetaPropertyUseCase metaPropertyUseCase, FDRPersistencePort port,
                          TransactionUseCase transactionUseCase, PassbookUseCase passbookUseCase,
                          ISavingsAccountUseCase savingsAccountUseCase, CalendarUseCase calendarUseCase,
                          AccruedInterestUseCase accruedInterestUseCase,
                          ManagementProcessTrackerUseCase managementProcessTrackerUseCase, FDRClosurePersistencePort fdrClosurePersistencePort, ModelMapper modelMapper) {
        this.commonRepository = commonRepository;
        this.holidayUseCase = holidayUseCase;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.port = port;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.calendarUseCase = calendarUseCase;
        this.accruedInterestUseCase = accruedInterestUseCase;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.rxtx = rxtx;
        this.transactionUseCase = transactionUseCase;
        this.passbookUseCase = passbookUseCase;
            this.fdrClosurePersistencePort = fdrClosurePersistencePort;
            this.modelMapper = modelMapper;
        }

        @Override
        public Mono<FDRResponseDTO> getFDRSchedule(String savingsAccountId) {
        return port
                .getSchedule(savingsAccountId)
                .collectList()
                .map(fdrScheduleList -> fdrScheduleList.isEmpty()
                        ? FDRResponseDTO
                                .builder()
                                .fdrScheduleInterestPostingList(null)
                                .message("Schedule Not Found For " + savingsAccountId)
                                .build()
                        : FDRResponseDTO
                                .builder()
                                .fdrScheduleInterestPostingList(fdrScheduleList)
                                .message("Schedule Found For " + savingsAccountId)
                                .build());
        }

        private Mono<List<FDRSchedule>> generateSchedule(String savingsAccountId, String loginId,
                LocalDate activationDate) {

        return holidayUseCase
                        .getAllHolidaysOfASamityBySavingsAccountId(savingsAccountId)
                        .map(HolidayResponseDTO::getHolidayDate)
                        .collectList()
                        .flatMap(holidayList -> commonRepository
                                .getFDRInterestCalculationEntityBySavingsAccountId(savingsAccountId)
                                .flatMap(fdrInterestCalculationEntity -> metaPropertyUseCase
                                        .getCalculationMetaProperty()
                                        .doOnNext(calculationMetaProperty -> log.info(
                                                        "Calculation Meta Property received : {}",
                                                        calculationMetaProperty))
                                        .flatMap(calculationMetaProperty ->
                                                getInterestRateAndDepositTermTuple(
                                                        fdrInterestCalculationEntity,
                                                        calculationMetaProperty)
                                                .map(tuple -> getFDRInterestPostingSchedule(
                                                                holidayList,
                                                                fdrInterestCalculationEntity,
                                                                tuple, loginId,
                                                                activationDate))
                                                .doOnNext(fdrScheduleList -> log.info(
                                                                "FDR Schedule List : {}",
                                                                        fdrScheduleList)))));
        }

        @Override
        public Flux<FDRSchedule> getFDRInterestPostingSchedulesByDateAndStatus(LocalDate interestPostingDate,
                String status) {
        return port.getFDRInterestPostingSchedulesByDateAndStatus(interestPostingDate, status);
        }

        @Override
        public Mono<FDRSchedule> updateScheduleStatus(String savingsAccountId, LocalDate interestPostingDate,
                String updatedStatus) {
        return port.updateScheduleStatus(savingsAccountId, interestPostingDate, updatedStatus);
        }

        @Override
        public Flux<FDRSchedule> postFDRInterestToAccount(LocalDate interestPostingDate, String loginId, String officeId) {
        return port.getFDRInterestPostingSchedulesByDateAndStatus(interestPostingDate,
                        Status.STATUS_PENDING.getValue())
                        .doOnNext(fdrSchedule -> log.info("fdr schedule received : {}", fdrSchedule))
                        .flatMap(fdrSchedule -> port
                                .checkIfLastInterestPosting(fdrSchedule.getSavingsAccountId(),
                                                fdrSchedule.getPostingNo())
                                .flatMap(aBoolean -> aBoolean
                                        ? savingsAccountUseCase
                                                        .updateFDRSavingsAccountOnMaturity(
                                                                        fdrSchedule.getSavingsAccountId())
                                                        .thenReturn(true)
                                        : Mono.just(false))
                                .then(transactionUseCase
                                        .createTransactionForSingleFDRInterest(loginId,
                                                        fdrSchedule.getSavingsAccountId(),
                                                        interestPostingDate,
                                                        fdrSchedule.getCalculatedInterest(), officeId))
                                .flatMap(transactionResponseDTO -> passbookUseCase
                                        .createPassbookEntryForInterestDeposit(
                                                        buildPassbookRequestDTO(
                                                                        transactionResponseDTO)))
                                .flatMap(accruedInterestResponseDTO -> port.updateScheduleStatus(
                                                fdrSchedule.getSavingsAccountId(),
                                                fdrSchedule.getInterestPostingDate(),
                                                Status.STATUS_PAID.getValue())))
                        .as(this.rxtx::transactional);

        }

        private Mono<BigDecimal> calculateFDRInterestPerMonth(String savingsAccountId, Double interestRatePerMonth) {
        return passbookUseCase.getLastPassbookEntryBySavingsAccount(savingsAccountId)
                        .map(PassbookResponseDTO::getSavgAcctEndingBalance)
                        .map(savgAcctEndingBalance -> savgAcctEndingBalance
                                        .multiply(BigDecimal.valueOf(interestRatePerMonth)));

        }

        @Override
        public Mono<FDRResponseDTO> activateFDRAccount(FDRRequestDTO requestDTO) {
        String savingsAccountId = requestDTO.getSavingsAccountId();
        BigDecimal fdrAmount = requestDTO.getFdrAmount();
        LocalDate activationDate = requestDTO.getActivationDate();
        String loginId = requestDTO.getLoginId();

        return savingsAccountUseCase
                        .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND,
                                        ExceptionMessages.SAVINGS_ACCOUNT_NOT_FOUND.getValue())))
                        .flatMap(savingsAccountResponseDTO -> validateSavingsAccount(savingsAccountResponseDTO,
                                        fdrAmount))
                        .flatMap(aBoolean -> transactionUseCase
                                        .createTransactionForFDRActivation(savingsAccountId, fdrAmount,
                                                        activationDate, loginId))
                        .flatMap(transactionResponseDTO -> passbookUseCase
                                        .createPassbookEntryForSavings(
                                                        buildPassbookRequestDTO(transactionResponseDTO)))
                        .flatMap(passbookResponseDTOList -> port
                                        .checkIfScheduleExistsBySavingsAccountId(
                                                        requestDTO.getSavingsAccountId())
                                        .flatMap(aBoolean -> aBoolean
                                                        ? Mono.error(new ExceptionHandlerUtil(
                                                                        HttpStatus.BAD_REQUEST,
                                                                        ExceptionMessages.SCHEDULE_ALREADY_EXISTS_FOR
                                                                                        .getValue()
                                                                                        + requestDTO.getSavingsAccountId()))
                                                        : generateSchedule(savingsAccountId, loginId,
                                                                        activationDate)))
                        .flatMap(port::saveInterestPostingSchedule)
                        .flatMap(fdrScheduleList -> savingsAccountUseCase
                                        .updateFDRSavingsAccountStatus(savingsAccountId,
                                                        Status.STATUS_ACTIVE.getValue(),
                                                        activationDate,
                                                        fdrScheduleList.get(fdrScheduleList.size() - 1)
                                                                        .getInterestPostingDate())
                                        .thenReturn(fdrScheduleList))
                        .map(fdrScheduleList -> FDRResponseDTO
                                        .builder()
                                        .fdrScheduleInterestPostingList(fdrScheduleList)
                                        .message("FDR Account Activated.")
                                        .build())
                        .as(this.rxtx::transactional);
        }

        @Override
        public Mono<String> activateFDRAccount2(FDRRequestDTO requestDTO) {

        String savingsAccountId = requestDTO.getSavingsAccountId();
        BigDecimal fdrAmount = requestDTO.getFdrAmount();
        LocalDate activationDate = requestDTO.getActivationDate();
        String loginId = requestDTO.getLoginId();

        return savingsAccountUseCase
                        .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND,
                                        ExceptionMessages.SAVINGS_ACCOUNT_NOT_FOUND.getValue())))
                        .flatMap(savingsAccountResponseDTO -> validateSavingsAccount(savingsAccountResponseDTO,
                                        fdrAmount)
                                .flatMap(aBoolean -> transactionUseCase
                                        .createTransactionForFDRActivation(savingsAccountId,
                                                        fdrAmount, activationDate,
                                                        loginId))
                                .flatMap(transactionResponseDTO -> passbookUseCase
                                        .createPassbookEntryForSavings(buildPassbookRequestDTO(
                                                        transactionResponseDTO)))
                                .flatMap(passbookResponseDTOList -> savingsAccountUseCase
                                        .updateFDRSavingsAccountStatus(savingsAccountId,
                                                        Status.STATUS_ACTIVE.getValue(),
                                                        activationDate,
                                                        getAccountEndDate(activationDate,
                                                                        savingsAccountResponseDTO
                                                                                        .getDepositTerm(),
                                                                        savingsAccountResponseDTO
                                                                                        .getDepositTermPeriod()))))
                        .map(savingsAccountResponseDTO -> "FDR Account Activated.")
                        .as(this.rxtx::transactional);
        }

        @Override
        public Mono<String> accrueFDRInterest(LocalDate interestCalculationDate, String officeId, String loginId) {

        /*
         * is called on every month end (along with GS accounts)
         * is called for every working days in corresponding month
         * get eligible fdr accounts whose acctStartDate(day of month) coincides between
         * current businessDate & last businessDate
         * get fdr interest calculation entity & calculation meta property
         * calculate monthly accrued interest
         * save to db
         * add a column 'interest_posting_date' in accrued interest table
         * check if interest_posting_date is null on each interest calculation cycle
         * if null generate a date 'n-1' months apart from current date. where n is
         * number of months in a interest_posting_period
         * if not null
         * check if current date is before that date
         * if before -> get & set this date as interest_posting_date for this accrued
         * interest
         * if equal or after -> generate a new date 'n-1' months apart & set it
         * post interest to account according to interestPostingFrequency
         *
         *
         */
        return calendarUseCase
                .getLastBusinessDateForOffice(officeId, interestCalculationDate)
                .flatMapMany(lastBusinessDate -> savingsAccountUseCase
                        .getAllFDRAccountsEligibleForInterestPosting(lastBusinessDate,
                                        interestCalculationDate))
                .flatMap(savingsAccountResponseDTO -> commonRepository
                        .getFDRInterestCalculationEntityBySavingsAccountId(
                                        savingsAccountResponseDTO.getSavingsAccountId())
                        .flatMap(fdrInterestCalculationEntity -> metaPropertyUseCase
                                .getCalculationMetaProperty()
                                .doOnNext(calculationMetaProperty -> log.info(
                                                "Calculation Meta Property received : {}",
                                                calculationMetaProperty))
                                .flatMap(calculationMetaProperty ->
                                        getInterestRatePerMonth(fdrInterestCalculationEntity, calculationMetaProperty)
                                                .flatMap(interestRatePerMonth -> calculateFDRInterestPerMonth(
                                                                savingsAccountResponseDTO
                                                                                .getSavingsAccountId(),
                                                                interestRatePerMonth))))
                        .flatMap(bigDecimal -> managementProcessTrackerUseCase
                                .getLastManagementProcessIdForOffice(officeId)
                                .flatMap(managementProcessId -> accruedInterestUseCase
                                        .saveFDRAccruedInterest(
                                        buildAccruedInterestCommand(
                                                        savingsAccountResponseDTO.getSavingsAccountId(),
                                                        loginId,
                                                        interestCalculationDate,
                                                        bigDecimal,
                                                        managementProcessId)))))
                .collectList()
                .map(savingsAccruedInterestResponseDTO -> "FDR Interest Accrued Successfully for Office : "
                                + officeId);
        }

        @Override
        public Mono<FDRGridViewDTO> getFDRGridViewByOffice(FDRGridViewCommand command) {
                return savingsAccountUseCase
                        .getFDRSavingsAccountsByOfficeId(command.getOfficeId())
                        .filter(fdrAccountDTO -> Strings.isNullOrEmpty(command.getSearchText())
                                || fdrAccountDTO.getMemberId().equalsIgnoreCase(command.getSearchText())
                                || fdrAccountDTO.getSavingsAccountId().equalsIgnoreCase(command.getSearchText()))
                        .sort(Comparator.comparing(FDRAccountDTO::getAcctStartDate).reversed())
                        .map(this::buildFDRGridView)
                        .collectList()
                        .flatMap(fdrList -> Flux.fromIterable(fdrList)
                                .skip((long) command.getOffset() * command.getLimit())
                                .take(command.getLimit())
                                .collectList()
                                .zipWith(managementProcessTrackerUseCase.getLastManagementProcessForOffice(command.getOfficeId()))
                                .map(tuple -> buildFDRGridViewDTO(tuple.getT1(), tuple.getT2(), fdrList.size()))
                        );
        }

        @Override
        public Mono<FDRDetailViewDTO> getFDRDetailViewByAccountId(String savingsAccountId) {
                return savingsAccountUseCase
                        .getFDRAccountDetailsBySavingsAccountId(savingsAccountId)
                        .map(this::buildFDRDetailView)
                        .map(this::buildFDRDetailViewDTO)
                        .flatMap(fdrDetailViewDTO -> fdrClosurePersistencePort
                                .checkIfFDRClosureExistsBySavingsAccountId(fdrDetailViewDTO.getData().getSavingsAccountId())
                                .flatMap(aBoolean -> {
                                Mono<FDRDetailViewDTO> fdrDetailViewDTOMono ;
                                    if (aBoolean) {
                                            fdrDetailViewDTOMono = fdrClosurePersistencePort
                                            .getFDRClosureBySavingsAccountId(savingsAccountId)
                                                .map(fdrClosure -> {
                                                    fdrDetailViewDTO.getData().setAcctCloseDate(fdrClosure.getAcctCloseDate());
                                                    fdrDetailViewDTO.getData().setClosingAmount(fdrClosure.getClosingAmount());
                                                    fdrDetailViewDTO.getData().setTotalInterest(fdrClosure.getTotalInterest());
                                                    fdrDetailViewDTO.setBtnEncashEnabled("No");
                                                    return fdrDetailViewDTO;
                                                });
                                    } else {
                                        fdrDetailViewDTO.setBtnEncashEnabled("Yes");
                                        fdrDetailViewDTOMono = Mono.just(fdrDetailViewDTO);
                                    }
                                    return fdrDetailViewDTOMono;
                                }));
        }

        @Override
        public Mono<FDRClosureDTO> closeFDRAccount(FDRClosureCommand command) {
                 return savingsAccountUseCase
                        .getFDRAccountDetailsBySavingsAccountId(command.getSavingsAccountId())
                        .flatMap(fdrAccount -> Mono.just(fdrAccount.getStatus())
                                .filter(s -> !s.equalsIgnoreCase(Status.STATUS_CLOSED.getValue()))
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Account already closed!.")))
                                /*.then(fdrClosurePersistencePort
                                        .checkIfFDRClosureExistsBySavingsAccountId(command.getSavingsAccountId()))*/
                                .then(fdrClosurePersistencePort
                                        .getFDRClosureBySavingsAccountId(command.getSavingsAccountId())
                                        .switchIfEmpty(Mono.just(FDRClosure.builder().build()))
                                        .flatMap(fdrClosure ->
                                                fdrClosure.getSavingsAccountId() == null || fdrClosure.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())
                                                        ? Mono.just(true)
                                                        : Mono.just(false))
                                .flatMap(aBoolean -> aBoolean
                                        ? passbookUseCase
                                        .getLastPassbookEntryBySavingsAccount(fdrAccount.getSavingsAccountId())
                                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Passbook Entry found for : "+ fdrAccount.getSavingsAccountId())))
                                        .flatMap(passbookResponseDTO -> command.getMaturityAmount() != null && command.getMaturityAmount().compareTo(BigDecimal.ZERO) > 0
                                                ? this.getTotalInterestAndMaturityAmountForFDRClosureWhenMaturityAmountProvided(passbookResponseDTO, fdrAccount, command)
                                                : this.getTotalInterestAndMaturityAmountForFDRClosure(passbookResponseDTO, fdrAccount, command))
                                        : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Closure Request already exists by SavingsAccountId : " + command.getSavingsAccountId())))
                                .flatMap(fdrClosure-> buildFDRClosure(fdrClosure, fdrAccount, command))))
                        .flatMap(fdrClosurePersistencePort::saveFDRClosure)
                        .map(fdrClosure -> FDRClosureDTO
                                .builder()
                                .userMessage("FDR Closure build successful")
                                .data(fdrClosure)
                                .build());
        }


        Mono<FDRClosure> getTotalInterestAndMaturityAmountForFDRClosureWhenMaturityAmountProvided(PassbookResponseDTO lastPassbookEntry, FDRAccountDTO fdrAccountDTO, FDRClosureCommand command) {

                return Mono.just(lastPassbookEntry)
                        .map(lastPassbookEnry ->
                                FDRClosure
                                        .builder()
                                        .closingInterest(lastPassbookEnry.getTotalDepositAmount() != null ? command.getMaturityAmount().subtract(lastPassbookEnry.getTotalDepositAmount()) : command.getMaturityAmount())
                                        .totalInterest(lastPassbookEnry.getTotalDepositAmount() != null ? command.getMaturityAmount().subtract(lastPassbookEnry.getTotalDepositAmount()) : command.getMaturityAmount())
                                        .closingAmount(command.getMaturityAmount())
                                        .actualInterestRate(fdrAccountDTO.getInterestRate())
                                        .effectiveInterestRate(getEffectiveInterestRateForFDR(command, fdrAccountDTO))
                                        .interestRateFrequency(fdrAccountDTO.getInterestRateFrequency())
                                        .interestPostingPeriod(fdrAccountDTO.getInterestPostingPeriod())
                                        .interestCompoundingPeriod(fdrAccountDTO.getInterestCompoundingPeriod())
                                        .build());
        }


        @Override
        public Mono<FDRClosureGridViewResponse> getFDRClosureGridViewByOffice(FDRGridViewCommand command) {
                AtomicReference<Integer> totalCount = new AtomicReference<>(0);
                return fdrClosurePersistencePort
                        .getAllFDRClosureByOfficeId(command.getOfficeId())
                        .doOnRequest(l -> log.info("requesting to fetch all fdr closures by office : {}", command.getOfficeId()))
                        .filter(fdrClosure -> fdrClosure.getStatus().equalsIgnoreCase(Status.STATUS_PENDING_APPROVAL.getValue()))
                        .filter(fdrClosure -> Strings.isNullOrEmpty(command.getSearchText())
                                || fdrClosure.getMemberId().equalsIgnoreCase(command.getSearchText())
                                || fdrClosure.getSavingsAccountId().equalsIgnoreCase(command.getSearchText()))
                        .map(fdrClosure -> modelMapper.map(fdrClosure, FDRClosureGridView.class))
                        .collectList()
                        .flatMapMany(fdrClosureGridViewList -> {
                                totalCount.set(fdrClosureGridViewList.size());
                                return Flux.fromIterable(fdrClosureGridViewList)
                                        .sort(Comparator.comparing(FDRClosureGridView::getAcctCloseDate).reversed())
                                        .skip((long) command.getOffset() * command.getLimit())
                                        .take(command.getLimit());
                        })
                        .collectList()
                        .doOnSuccess(fdrClosureGridViewList -> log.info("successfully fetched fdr closure list by office : {}. FDR Closure List : {}", command.getOfficeId(), fdrClosureGridViewList))
                        .map(fdrClosureGridViewList -> FDRClosureGridViewResponse
                                .builder()
                                .userMessage("FDR Closure Pending List Fetched Successfully.")
                                .data(fdrClosureGridViewList)
                                .totalCount(totalCount.get())
                                .build());
        }

        @Override
        public Mono<FDRClosureDetailViewResponse> getFDRClosureDetailViewBySavingsAccountId(String savingsAccountId) {
                return savingsAccountUseCase
                        .getFDRAccountDetailsBySavingsAccountId(savingsAccountId)
                        .zipWith(fdrClosurePersistencePort.getFDRClosureBySavingsAccountId(savingsAccountId))
                        .map(this::buildFDRAuthorizationDetailView)
                        .map(fdrClosureDetailView ->
                                FDRClosureDetailViewResponse
                                        .builder()
                                        .userMessage("FDR Closure Detail Fetched Successfully.")
                                        .data(fdrClosureDetailView)
                                        .build());
        }

        @Override
        public Mono<FDRClosureDTO> getFDRClosingInfoBySavingsAccountId(FDRClosureCommand command) {
                return savingsAccountUseCase
                        .getFDRAccountDetailsBySavingsAccountId(command.getSavingsAccountId())
                        .flatMap(fdrAccountDTO -> Mono.just(fdrAccountDTO.getStatus())
                                .filter(s -> !s.equalsIgnoreCase(Status.STATUS_CLOSED.getValue()))
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Account already closed!.")))
                                .then(fdrClosurePersistencePort
                                        .getFDRClosureBySavingsAccountId(command.getSavingsAccountId())
                                        .switchIfEmpty(Mono.just(FDRClosure.builder().build()))
                                        .flatMap(fdrClosure ->
                                                fdrClosure.getSavingsAccountId() == null || fdrClosure.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())
                                                        ? Mono.just(true)
                                                        : Mono.just(false))
                                        .flatMap(aBoolean -> aBoolean
                                                ? passbookUseCase
                                                .getLastPassbookEntryBySavingsAccount(fdrAccountDTO.getSavingsAccountId())
                                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Passbook Entry found for : "+ fdrAccountDTO.getSavingsAccountId())))
                                                .flatMap(passbookResponseDTO -> this.getTotalInterestAndMaturityAmountForFDRClosureInfo(passbookResponseDTO, fdrAccountDTO, command))
                                                : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Closure Request already exists by SavingsAccountId : " + command.getSavingsAccountId())))
                                        .flatMap(fdrClosure -> buildFDRClosure(fdrClosure, fdrAccountDTO, command))))
//                .flatMap(port::saveDPSClosure)
                        .map(fdrClosure -> FDRClosureDTO
                                .builder()
                                .userMessage("FDR Closure Info Fetch successful.")
                                .data(fdrClosure)
                                .build());
        }


        Mono<FDRClosure> getTotalInterestAndMaturityAmountForFDRClosureInfo(PassbookResponseDTO lastPassbookEntry, FDRAccountDTO fdrAccountDTO, FDRClosureCommand command) {
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
                                ? Mono.just(calculateInterestWhenLastPassbookEntryIsInterestPosting(lastPassbookEntry, fdrAccountDTO, command))
                                : passbookUseCase
                                .getLastInterestDepositPassbookEntryBySavingsAccountOid(fdrAccountDTO.getSavingsAccountOid())
                                .switchIfEmpty(Mono.just(lastPassbookEntry))
                                .map(PassbookResponseDTO::getTransactionDate)
                                .flatMap(lastInterestPostingDate -> passbookUseCase.getPassbookEntriesBetweenTransactionDates(command.getSavingsAccountId(), lastInterestPostingDate, command.getEncashmentDate()))
                                .flatMap(passbookList -> Mono.just(calculateInterestWhenPassbookEntryExistsAfterLastInterestPosting(passbookList, fdrAccountDTO, command))))
                        .map(totalInterestAfterLastInterestPosting ->
                                FDRClosure
                                        .builder()
                                        .closingInterest(totalInterestAfterLastInterestPosting)
                                        .totalInterest(lastPassbookEntry.getTotalAccruedInterDeposit() == null
                                                ? totalInterestAfterLastInterestPosting
                                                : lastPassbookEntry.getTotalAccruedInterDeposit().add(totalInterestAfterLastInterestPosting))
                                        .closingAmount(lastPassbookEntry.getSavgAcctEndingBalance().add(totalInterestAfterLastInterestPosting))
                                        .actualInterestRate(fdrAccountDTO.getInterestRate())
                                        .effectiveInterestRate(getEffectiveInterestRateForFDR(command, fdrAccountDTO))
                                        .interestRateFrequency(fdrAccountDTO.getInterestRateFrequency())
                                        .interestPostingPeriod(fdrAccountDTO.getInterestPostingPeriod())
                                        .interestCompoundingPeriod(fdrAccountDTO.getInterestCompoundingPeriod())
                                        .build());
        }


        private FDRClosureDetailView buildFDRAuthorizationDetailView(Tuple2<FDRAccountDTO, FDRClosure> fdrAccountFDRClosureTuple) {
                FDRAccountDTO fdrAccountDTO = fdrAccountFDRClosureTuple.getT1();
                FDRClosure fdrClosure = fdrAccountFDRClosureTuple.getT2();

                return FDRClosureDetailView
                        .builder()
                        .savingsAccountId(fdrClosure.getSavingsAccountId())
                        .savingsApplicationId(fdrClosure.getSavingsApplicationId())
                        .savingsProductId(fdrClosure.getSavingsProductId())
                        .savingsProdNameEn(fdrClosure.getSavingsProdNameEn())
                        .memberId(fdrClosure.getMemberId())
                        .memberNameEn(fdrClosure.getMemberNameEn())
                        .savingsAmount(fdrClosure.getSavingsAmount())
                        .interestRateFrequency(fdrClosure.getInterestRateFrequency())
                        .interestRateTerms("Fixed")
                        .interestPostingPeriod(fdrClosure.getInterestPostingPeriod())
                        .interestCompoundingPeriod(fdrClosure.getInterestCompoundingPeriod())
                        .acctStartDate(fdrClosure.getAcctStartDate())
                        .acctEndDate(fdrClosure.getAcctEndDate())
                        .acctCloseDate(fdrClosure.getAcctCloseDate())
                        .closingAmount(fdrClosure.getClosingAmount())
                        .totalInterest(fdrClosure.getTotalInterest())
                        .effectiveInterestRate(fdrClosure.getEffectiveInterestRate())
                        .status(fdrClosure.getStatus())
                        .interestRate(fdrAccountDTO.getInterestRate())
                        .maturityAmount(fdrAccountDTO.getMaturityAmount())
                        .build();
        }


        Mono<FDRClosure> getTotalInterestAndMaturityAmountForFDRClosure(PassbookResponseDTO lastPassbookEntry, FDRAccountDTO fdrAccount, FDRClosureCommand command) {
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
                                ? Mono.just(calculateInterestWhenLastPassbookEntryIsInterestPosting(lastPassbookEntry, fdrAccount, command))
                                : passbookUseCase
                                        .getLastInterestDepositPassbookEntryBySavingsAccountOid(fdrAccount.getSavingsAccountOid())
                                        .switchIfEmpty(Mono.just(lastPassbookEntry))
                                        .map(PassbookResponseDTO::getTransactionDate)
                                        .flatMap(lastInterestPostingDate -> passbookUseCase.getPassbookEntriesBetweenTransactionDates(command.getSavingsAccountId(), lastInterestPostingDate, command.getEncashmentDate()))
                                        .flatMap(passbookList -> Mono.just(calculateInterestWhenPassbookEntryExistsAfterLastInterestPosting(passbookList, fdrAccount, command))))
                        .map(totalInterestAfterLastInterestPosting ->
                                FDRClosure
                                .builder()
                                .closingInterest(totalInterestAfterLastInterestPosting)
                                .totalInterest(lastPassbookEntry.getTotalAccruedInterDeposit() == null
                                        ? totalInterestAfterLastInterestPosting
                                        : lastPassbookEntry.getTotalAccruedInterDeposit().add(totalInterestAfterLastInterestPosting))
                                .closingAmount(lastPassbookEntry.getSavgAcctEndingBalance().add(totalInterestAfterLastInterestPosting))
                                .actualInterestRate(fdrAccount.getInterestRate())
                                .effectiveInterestRate(getEffectiveInterestRateForFDR(command, fdrAccount))
                                .interestRateFrequency(fdrAccount.getInterestRateFrequency())
                                .interestPostingPeriod(fdrAccount.getInterestPostingPeriod())
                                .interestCompoundingPeriod(fdrAccount.getInterestCompoundingPeriod())
                                .build());
        }

        BigDecimal calculateInterestWhenPassbookEntryExistsAfterLastInterestPosting(List<PassbookResponseDTO> passbookList, FDRAccountDTO fdrAccountDTO, FDRClosureCommand command) {
                passbookList.sort(Comparator.comparing(PassbookResponseDTO::getTransactionDate));
                BigDecimal effectiveInterestRate = getEffectiveInterestRateForFDR(command, fdrAccountDTO);
                BigDecimal effectiveInterestRatePerDay = effectiveInterestRate.divide(BigDecimal.valueOf(36500), 8,RoundingMode.UP);
                BigDecimal totalInterest = BigDecimal.ZERO;
                log.info("passbookList size : {}", passbookList.size());

                for (int i = 0; i < passbookList.size()-1; i++) {
                        LocalDate currentTransactionDate = passbookList.get(i).getTransactionDate();
                        LocalDate nextTransactionDate = passbookList.get(i+1).getTransactionDate();
                        long numberOfDays = Math.abs(ChronoUnit.DAYS.between(currentTransactionDate, nextTransactionDate));
                        BigDecimal savgAcctEndingBalanceBeforeNextTransaction = passbookList.get(i).getSavgAcctEndingBalance();
                        BigDecimal interestForThisPeriod = savgAcctEndingBalanceBeforeNextTransaction
                                                                .multiply(effectiveInterestRatePerDay)
                                                                .multiply(BigDecimal.valueOf(numberOfDays));
                        totalInterest = totalInterest.add(interestForThisPeriod);
                }

                if (passbookList.size() == 1) {
                        log.info("passbook list size is 1 & calculating total interest");
                        long numberOfDays = Math.abs(ChronoUnit.DAYS.between(passbookList.get(0).getTransactionDate(), command.getEncashmentDate()));
                        log.info("number of Days between {} & {} = {}", passbookList.get(0).getTransactionDate(), command.getEncashmentDate(), numberOfDays);
                        totalInterest = passbookList.get(0).getSavgAcctEndingBalance()
                                .multiply(effectiveInterestRatePerDay)
                                .multiply(BigDecimal.valueOf(numberOfDays));
                        log.info("total interest : {}", totalInterest);
                }

                totalInterest = CommonFunctions.round(totalInterest, 0, RoundingMode.HALF_UP);
                log.info("totalInterest : {}", totalInterest);


                return totalInterest;
        }

        BigDecimal calculateInterestWhenLastPassbookEntryIsInterestPosting(PassbookResponseDTO lastPassbookEntry, FDRAccountDTO fdrAccountDTO, FDRClosureCommand command) {
                BigDecimal effectiveInterestRate = getEffectiveInterestRateForFDR(command, fdrAccountDTO);
                BigDecimal effectiveInterestRatePerDay = effectiveInterestRate.divide(BigDecimal.valueOf(36500), 8,RoundingMode.UP);
                BigDecimal savingsAmount = lastPassbookEntry.getSavgAcctEndingBalance();

                log.info("lastPassbookEntry : {}", lastPassbookEntry);
                log.info("fdrAccountDTO : {}", fdrAccountDTO);
                log.info("effectiveInterestRate : {}", effectiveInterestRate);
                log.info("savgAcctEndingBalance : {}", savingsAmount);

                long numberOfDays = Math.abs(ChronoUnit.DAYS.between(command.getEncashmentDate(), lastPassbookEntry.getTransactionDate()));
                log.info("numberOfDays: {}", numberOfDays);

                BigDecimal interestCalculatedFromLastPostingUpToClosing = CommonFunctions.round(savingsAmount
                                .multiply(effectiveInterestRatePerDay)
                                .multiply(BigDecimal.valueOf(numberOfDays)),
                        0,
                        RoundingMode.HALF_UP);
                log.info("interestCalculatedFromLastPostingUpToClosing : {}", interestCalculatedFromLastPostingUpToClosing);

                return interestCalculatedFromLastPostingUpToClosing;
        }



        @Override
        public Mono<FDRClosureDTO> authorizeFDRClosure(FDRAuthorizeCommand command) {
                /*
                * get fdr closure dto by savings account id
                * change status to closed
                * create a transaction with status Approved & transaction Code FDR_CLOSURE
                * create passbook entry
                *       update balance to 0.00
                * */

                return validateFDRAccount(command.getSavingsAccountId())
                        .flatMap(valid -> valid
                                ? fdrClosurePersistencePort.getFDRClosureBySavingsAccountId(command.getSavingsAccountId())
                                .filter(fdrClosure -> fdrClosure.getStatus().equalsIgnoreCase(Status.STATUS_PENDING_APPROVAL.getValue()))
                                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Closure Application Cannot be Authorized : " + command.getSavingsAccountId())))
                                .flatMap(fdrClosure -> buildTransactionAndCreatePassbookEntries(fdrClosure, command))
                                .flatMap(passbookResponseDTO -> updateFDRClosureAndSavingsAccount(command.getSavingsAccountId(), command.getLoginId()))
                                .map(fdrClosure -> FDRClosureDTO.builder()
                                        .userMessage("FDR Closure Authorization Successful.")
                                        .data(fdrClosure)
                                        .build())
                                : Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "FDR Closure Application not found for : " + command.getSavingsAccountId())))
                        .as(this.rxtx::transactional);

        }

        private Mono<PassbookResponseDTO> buildTransactionAndCreatePassbookEntries(FDRClosure fdrClosure, FDRAuthorizeCommand command) {
                return buildTransactionForFDRClosure(fdrClosure, command)
                        .flatMap(transactionUseCase::createTransactionForFDRClosure)
                        .map(singleTransactionResponseDTO -> Tuples.of(singleTransactionResponseDTO, fdrClosure.getClosingInterest()))
                        .flatMap(transactionInterestTuple -> passbookUseCase.createPassbookEntryForInterestDeposit(
                                        buildPassbookRequestDTOForInterestPosting(transactionInterestTuple.getT1(), transactionInterestTuple.getT2(), command))
                                .flatMap(accruedInterestResponseDTO -> passbookUseCase.createPassbookEntryForTermDepositClosure(
                                        buildPassbookRequestDTOForFDRClosure(transactionInterestTuple.getT1(), command, accruedInterestResponseDTO))));
        }

        private Mono<FDRClosure> updateFDRClosureAndSavingsAccount(String savingsAccountId, String loginId) {
                return fdrClosurePersistencePort.updateFDRClosureStatus(savingsAccountId, Status.STATUS_APPROVED.getValue(), loginId)
                        .flatMap(fdrClosure ->  savingsAccountUseCase.updateSavingsAccountStatus(savingsAccountId, Status.STATUS_CLOSED.getValue(), loginId)
                                .thenReturn(fdrClosure));
        }

        @Override
        public Mono<FDRClosureDTO> rejectFDRClosure(FDRAuthorizeCommand command) {
                return fdrClosurePersistencePort
                        .getFDRClosureBySavingsAccountId(command.getSavingsAccountId())
                        .doOnRequest(l -> log.info("requesting to reject fdr closure application for savings account id : {}", command.getSavingsAccountId()))
                        .filter(fdrClosure -> fdrClosure.getStatus().equalsIgnoreCase(Status.STATUS_PENDING_APPROVAL.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Closure Application Cannot be Rejected : " + command.getSavingsAccountId())))
                        .map(fdrClosure -> {
                                fdrClosure.setStatus(Status.STATUS_REJECTED.getValue());
                                fdrClosure.setRejectedBy(command.getLoginId());
                                fdrClosure.setRejectedOn(LocalDateTime.now());
                                fdrClosure.setRemarks(command.getRemarks());
                                return fdrClosure;
                        })
                        .flatMap(fdrClosurePersistencePort::saveFDRClosure)
                        .map(fdrClosure -> FDRClosureDTO
                                .builder()
                                .userMessage("FDR Closure Rejected Successfully.")
                                .data(fdrClosure)
                                .build());
        }

        Mono<Boolean> validateFDRAccount(String savingsAccountId) {
                return savingsAccountUseCase
                        .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                        .map(SavingsAccountResponseDTO::getStatus)
                        .filter(s -> !s.equalsIgnoreCase(Status.STATUS_CLOSED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "FDR Account already closed!.")))
                        .then(fdrClosurePersistencePort
                                .checkIfFDRClosureExistsBySavingsAccountId(savingsAccountId));
        }

        PassbookRequestDTO buildPassbookRequestDTOForFDRClosure(SingleTransactionResponseDTO transaction, FDRAuthorizeCommand command, AccruedInterestResponseDTO accruedInterestResponseDTO) {
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

        Mono<Transaction> buildTransactionForFDRClosure(FDRClosure fdrClosure, FDRAuthorizeCommand command) {
                return managementProcessTrackerUseCase.getLastManagementProcessIdForOffice(command.getOfficeId())
                        .map(managementProcessId -> Transaction
                        .builder()
                        .managementProcessId(managementProcessId)
                        .processId(UUID.randomUUID().toString())
                        .accountType(ACCOUNT_TYPE_SAVINGS.getValue())
                        .savingsAccountId(fdrClosure.getSavingsAccountId())
                        .transactionId(UUID.randomUUID().toString())
                        .memberId(fdrClosure.getMemberId())
                        .amount(fdrClosure.getClosingAmount())
//                        .transactionCode(TRANSACTION_CODE_FDR_CLOSURE.getValue())
                        .transactionCode(TRANSACTION_CODE_SAVINGS_WITHDRAW.getValue())
                        .mfiId(command.getMfiId())
                        .officeId(command.getOfficeId())
                        .transactionDate(fdrClosure.getAcctCloseDate())
                        .transactedBy(command.getLoginId())
                        .createdOn(LocalDateTime.now())
                        .createdBy(command.getLoginId())
                        .paymentMode(fdrClosure.getPaymentMode())
                        .status(Status.STATUS_APPROVED.getValue())
                        .build());
        }

        Mono<FDRClosure> buildFDRClosure(FDRClosure fdrClosure, FDRAccountDTO fdrAccount, FDRClosureCommand command) {
                return managementProcessTrackerUseCase.getLastManagementProcessIdForOffice(command.getOfficeId())
                        .map(managementProcessId -> {
                                fdrClosure.setSavingsAccountId(command.getSavingsAccountId());
                                fdrClosure.setAcctStartDate(fdrAccount.getAcctStartDate());
                                fdrClosure.setAcctEndDate(fdrAccount.getAcctEndDate());
                                fdrClosure.setAcctCloseDate(command.getEncashmentDate());
                                fdrClosure.setPaymentMode(command.getPaymentMode());
                                fdrClosure.setSavingsAmount(fdrAccount.getSavingsAmount());
                                fdrClosure.setReferenceAccountId(command.getReferenceAccountId());

                                fdrClosure.setCreatedBy(command.getLoginId());
                                fdrClosure.setCreatedOn(LocalDateTime.now());
                                fdrClosure.setMemberId(fdrAccount.getMemberId());
                                fdrClosure.setMemberNameEn(fdrAccount.getMemberNameEn());
                                fdrClosure.setMemberNameBn(fdrAccount.getMemberNameBn());

                                fdrClosure.setSavingsApplicationId(fdrAccount.getSavingsApplicationId());
                                fdrClosure.setSavingsProductId(fdrAccount.getSavingsProductId());
                                fdrClosure.setSavingsProdNameEn(fdrAccount.getSavingsProdNameEn());

                                fdrClosure.setStatus(Status.STATUS_PENDING_APPROVAL.getValue());
                                fdrClosure.setOfficeId(command.getOfficeId());
                                fdrClosure.setManagementProcessId(managementProcessId);
                                fdrClosure.setSavingsTypeId(fdrAccount.getSavingsTypeId());

                                return fdrClosure;
                        });
        }


        BigDecimal getEffectiveInterestRateForFDR(FDRClosureCommand command, FDRAccountDTO fdrAccountDTO) {
                return command.getEffectiveInterestRate() == null
                        || command.getEncashmentDate().isAfter(fdrAccountDTO.getAcctEndDate())
                        || command.getEncashmentDate().isEqual(fdrAccountDTO.getAcctEndDate())
                        ? CommonFunctions.getAnnualInterestRate(fdrAccountDTO.getInterestRate(), fdrAccountDTO.getInterestRateFrequency())
                        : CommonFunctions.getAnnualInterestRate(command.getEffectiveInterestRate(), fdrAccountDTO.getInterestRateFrequency());
        }


        FDRClosure getTotalInterestAndMaturityAmountForFDREncashment(Tuple2<PassbookResponseDTO, FDRAccountDTO> passbookFDRTuple, FDRClosureCommand command, LocalDate lastInterestPostingDate) {
                PassbookResponseDTO lastPassbookEntry = passbookFDRTuple.getT1();
                FDRAccountDTO fdrAccount = passbookFDRTuple.getT2();
                BigDecimal effectiveInterestRate = getEffectiveInterestRateForFDR(command, fdrAccount);
                BigDecimal effectiveInterestRatePerDay = effectiveInterestRate.divide(BigDecimal.valueOf(36500), 8,RoundingMode.UP);
                BigDecimal savingsAmount = lastPassbookEntry.getSavgAcctEndingBalance();

                log.info("lastPassbookEntry : {}", lastPassbookEntry);
                log.info("fdrAccount : {}", fdrAccount);
                log.info("effectiveInterestRate : {}", effectiveInterestRate);
                log.info("savgAcctEndingBalance : {}", savingsAmount);
                log.info("lastInterestPostingDate : {}", lastInterestPostingDate);

                long numberOfDays = Math.abs(ChronoUnit.DAYS.between(command.getEncashmentDate(), lastInterestPostingDate));
                log.info("numberOfDays: {}", numberOfDays);

                BigDecimal interestCalculatedFromLastPostingUpToClosing = CommonFunctions.round(savingsAmount
                                .multiply(effectiveInterestRatePerDay)
                                .multiply(BigDecimal.valueOf(numberOfDays)),
                                0,
                                RoundingMode.HALF_UP);
                log.info("interestCalculatedFromLastPostingUpToClosing : {}", interestCalculatedFromLastPostingUpToClosing);

                BigDecimal totalInterest = lastPassbookEntry.getTotalAccruedInterDeposit() == null
                                ? interestCalculatedFromLastPostingUpToClosing
                                : interestCalculatedFromLastPostingUpToClosing.add(lastPassbookEntry.getTotalAccruedInterDeposit());
                log.info("totalInterest : {}", totalInterest);

                return FDRClosure
                        .builder()
                        .totalInterest(totalInterest)
                        .closingAmount(lastPassbookEntry.getSavgAcctEndingBalance().add(interestCalculatedFromLastPostingUpToClosing))
                        .actualInterestRate(fdrAccount.getInterestRate())
                        .effectiveInterestRate(effectiveInterestRate)
                        .interestRateFrequency(fdrAccount.getInterestRateFrequency())
                        .interestPostingPeriod(fdrAccount.getInterestPostingPeriod())
                        .interestCompoundingPeriod(fdrAccount.getInterestCompoundingPeriod())
                        .build();
        }




        private FDR buildFDRGridView(FDRAccountDTO fdrAccountDTO) {
                return FDR
                        .builder()
                        .savingsAccountId(fdrAccountDTO.getSavingsAccountId())
                        .memberId(fdrAccountDTO.getMemberId())
                        .memberNameEn(fdrAccountDTO.getMemberNameEn())
                        .memberNameBn(fdrAccountDTO.getMemberNameBn())
                        .acctStartDate(fdrAccountDTO.getAcctStartDate())
                        .savingsAmount(fdrAccountDTO.getSavingsAmount())
                        .acctEndDate(fdrAccountDTO.getAcctEndDate())
                        .maturityAmount(fdrAccountDTO.getMaturityAmount())
                        .status(fdrAccountDTO.getStatus())
                        .build();
        }

        private FDR buildFDRDetailView(FDRAccountDTO fdrAccountDTO) {
                ModelMapper mapper = new ModelMapper();
                return mapper.map(fdrAccountDTO, FDR.class);
        }

        private FDRGridViewDTO buildFDRGridViewDTO(List<FDR> fdrList, ManagementProcessTracker managementProcessTracker, Integer totalFDRAccounts) {
        return FDRGridViewDTO
                .builder()
                .officeId(managementProcessTracker.getOfficeId())
                .officeNameEn(managementProcessTracker.getOfficeNameEn())
                .officeNameBn(managementProcessTracker.getOfficeNameBn())
                .businessDate(managementProcessTracker.getBusinessDate())
                .businessDay(managementProcessTracker.getBusinessDay())
                .userMessage("FDR accounts fetched successfully")
                .data(fdrList)
                .totalCount(totalFDRAccounts)
                .build();
        }

        private FDRDetailViewDTO buildFDRDetailViewDTO(FDR fdr) {
                return FDRDetailViewDTO
                        .builder()
                        .userMessage("FDR account detail fetched successfully")
                        .data(fdr)
                        .build();
        }

        private AccruedInterestCommand buildAccruedInterestCommand(String savingsAccountId, String loginId,
                LocalDate interestCalculationDate, BigDecimal fdrInterest, String managementProcessId) {

        return AccruedInterestCommand
                        .builder()
                        .savingsAccountId(savingsAccountId)
                        .managementProcessId(managementProcessId)
                        .interestCalculationDate(interestCalculationDate)
                        .interestCalculationMonth(interestCalculationDate.getMonthValue())
                        .interestCalculationYear(interestCalculationDate.getYear())
                        .loginId(loginId)
                        .fdrInterest(fdrInterest)
                        .build();
        }

        private LocalDate getAccountEndDate(LocalDate accountActivationDate, Integer depositTerm,
                String depositTermPeriod) {
        Integer depositTermInMonths = getDepositTermInMonths(depositTerm, depositTermPeriod);
        return accountActivationDate.plusMonths(depositTermInMonths);
        }

        private Mono<Boolean> validateSavingsAccount(SavingsAccountResponseDTO savingsAccountResponseDTO,
                BigDecimal fdrAmount) {

        if (savingsAccountResponseDTO.getStatus().equalsIgnoreCase(Status.STATUS_ACTIVE.getValue()))
                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                                ExceptionMessages.ACCOUNT_ALREADY_ACTIVATED.getValue()));
        else if (fdrAmount.compareTo(savingsAccountResponseDTO.getMinDepositAmount()) < 0)
                return Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT,
                                ExceptionMessages.MINIMUM_AMOUNT_REQUIREMENT_NOT_MET.getValue()));
        else if (fdrAmount.compareTo(savingsAccountResponseDTO.getMaxDepositAmount()) > 0)
                return Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT,
                                ExceptionMessages.AMOUNT_EXCEEDED_MAXIMUM_AMOUNT.getValue()));
        else if (savingsAccountResponseDTO.getSavingsAmount().compareTo(fdrAmount) != 0)
                return Mono.error(
                                new ExceptionHandlerUtil(HttpStatus.CONFLICT,
                                                ExceptionMessages.FDR_AMOUNT_MISMATCH.getValue()));
        else if (savingsAccountResponseDTO.getStatus().equalsIgnoreCase(Status.STATUS_INACTIVE.getValue())
                        && savingsAccountResponseDTO.getSavingsAmount().compareTo(fdrAmount) == 0)
                return Mono.just(true);
        else
                return Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT,
                                ExceptionMessages.ACCOUNT_NOT_ELIGIBLE_FOR_ACTIVATION.getValue()));

        }

        private PassbookRequestDTO buildPassbookRequestDTO(SingleTransactionResponseDTO transactionResponseDTO) {
        return PassbookRequestDTO
                        .builder()
                        .amount(transactionResponseDTO.getAmount())
                        .managementProcessId(transactionResponseDTO.getManagementProcessId() != null
                                        ? transactionResponseDTO.getManagementProcessId()
                                        : null)
                        .processId(transactionResponseDTO.getProcessId() != null
                                        ? transactionResponseDTO.getProcessId()
                                        : null)
                        .savingsAccountId(transactionResponseDTO.getSavingsAccountId())
                        .transactionId(transactionResponseDTO.getTransactionId())
                        .transactionCode(transactionResponseDTO.getTransactionCode())
                        .mfiId(transactionResponseDTO.getMfiId())
                        .loginId(transactionResponseDTO.getLoginId())
                        .transactionDate(transactionResponseDTO.getTransactionDate())
                        .paymentMode(transactionResponseDTO.getPaymentMode())
                        .memberId(transactionResponseDTO.getMemberId())
                        .samityId(transactionResponseDTO.getSamityId())
                        .build();
        }

        private PassbookRequestDTO buildPassbookRequestDTOForInterestPosting(SingleTransactionResponseDTO transactionResponseDTO, BigDecimal totalInterest, FDRAuthorizeCommand command) {
                PassbookRequestDTO requestDTO = PassbookRequestDTO
                        .builder()
                        .amount(totalInterest)
                        .managementProcessId(transactionResponseDTO.getManagementProcessId() != null
                                ? transactionResponseDTO.getManagementProcessId()
                                : null)
                        .processId(transactionResponseDTO.getProcessId() != null
                                ? transactionResponseDTO.getProcessId()
                                : null)
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

        public List<FDRSchedule> getFDRInterestPostingSchedule(List<LocalDate> holidayList,
                FDRInterestCalculationEntity fdrInterestCalculationEntity,
                Tuple2<Double, Integer> tupleOfInterestRatePerPostingPeriodAndDepositTermInMonths,
                String loginId,
                LocalDate activationDate) {

        List<FDRSchedule> fdrScheduleList = new ArrayList<>();
        int noOfInterestPosting;
        int monthsToAdd = getMonthsToAdd(fdrInterestCalculationEntity.getInterestPostingPeriod());
        Integer termInMonths = tupleOfInterestRatePerPostingPeriodAndDepositTermInMonths.getT2();
        BigDecimal savingsAmount = fdrInterestCalculationEntity.getSavingsAmount();
        BigDecimal interestRatePerPostingPeriod = BigDecimal
                        .valueOf(tupleOfInterestRatePerPostingPeriodAndDepositTermInMonths.getT1());
        noOfInterestPosting = termInMonths / monthsToAdd;

        BigDecimal calculatedInterest = savingsAmount.multiply(interestRatePerPostingPeriod).setScale(2,
                        RoundingMode.HALF_UP);

        for (int i = 1; i <= noOfInterestPosting; i++) {
                LocalDate interestPostingDate = activationDate.plusMonths((long) i * monthsToAdd);

                while (holidayList.contains(interestPostingDate)) {
                        interestPostingDate = interestPostingDate.plusDays(1);
                }

                fdrScheduleList.add(FDRSchedule
                                .builder()
                                .savingsAccountId(fdrInterestCalculationEntity.getSavingsAccountId())
                                .postingNo(i)
                                .interestPostingDate(interestPostingDate)
                                .calculatedInterest(calculatedInterest)
                                .status(Status.STATUS_PENDING.getValue())
                                .createdOn(LocalDateTime.now())
                                .createdBy(loginId)
                                .build());
        }
        return fdrScheduleList;
        }

        private Integer getMonthsToAdd(String interestPostingPeriod) {
        int monthsToAdd = 1;

        if (interestPostingPeriod.equalsIgnoreCase(FDREnum.INTEREST_POSTING_PERIOD_QUARTERLY.getValue()))
                monthsToAdd = 3;
        else if (interestPostingPeriod.equalsIgnoreCase(FDREnum.INTEREST_POSTING_PERIOD_HALF_YEARLY.getValue()))
                monthsToAdd = 6;
        else if (interestPostingPeriod.equalsIgnoreCase(FDREnum.INTEREST_POSTING_PERIOD_YEARLY.getValue()))
                monthsToAdd = 12;

        return monthsToAdd;
        }

        private Mono<Tuple2<Double, Integer>> getInterestRateAndDepositTermTuple(
                FDRInterestCalculationEntity fdrInterestCalculationEntity,
                CalculationMetaProperty calculationMetaProperty) {
        return Mono.just(Tuples.of(
                        getInterestRatePerPostingPeriod(fdrInterestCalculationEntity.getInterestRate() / 100,
                                        fdrInterestCalculationEntity.getInterestRateFrequency(),
                                        fdrInterestCalculationEntity.getInterestPostingPeriod(),
                                        calculationMetaProperty),
                        getDepositTermInMonths(fdrInterestCalculationEntity.getDepositTerm(),
                                        fdrInterestCalculationEntity.getDepositTermPeriod())));
        }

        private Mono<Double> getInterestRatePerMonth(FDRInterestCalculationEntity fdrInterestCalculationEntity,
                CalculationMetaProperty calculationMetaProperty) {
        return Mono.just(getMonthlyInterestRate(fdrInterestCalculationEntity.getInterestRate(),
                        fdrInterestCalculationEntity.getInterestRateFrequency(), calculationMetaProperty));
        }

        private Double getInterestRatePerPostingPeriod(Double interestRate, String interestRateFrequency,
                String interestPostingPeriod, CalculationMetaProperty calculationMetaProperty) {

        Double interestRatePerPeriod = null;
        if (interestPostingPeriod.equalsIgnoreCase(FDREnum.INTEREST_POSTING_PERIOD_MONTHLY.getValue()))
                interestRatePerPeriod = getMonthlyInterestRate(interestRate, interestRateFrequency,
                                calculationMetaProperty);
        else if (interestPostingPeriod.equalsIgnoreCase(FDREnum.INTEREST_POSTING_PERIOD_QUARTERLY.getValue()))
                interestRatePerPeriod = getQuarterlyInterestRate(interestRate, interestRateFrequency,
                                calculationMetaProperty);
        else if (interestPostingPeriod.equalsIgnoreCase(FDREnum.INTEREST_POSTING_PERIOD_YEARLY.getValue()))
                interestRatePerPeriod = getYearlyInterestRate(interestRate, interestRateFrequency,
                                calculationMetaProperty);
        else if (interestPostingPeriod.equalsIgnoreCase(FDREnum.INTEREST_POSTING_PERIOD_HALF_YEARLY.getValue()))
                interestRatePerPeriod = getHalfYearlyInterestRate(interestRate, interestRateFrequency,
                                calculationMetaProperty);

        return interestRatePerPeriod;
        }

        private Integer getDepositTermInMonths(Integer depositTerm, String depositTermPeriod) {
        Integer numberOfMonths = null;
        if (depositTermPeriod.equalsIgnoreCase(FDREnum.DEPOSIT_TERM_PERIOD_MONTH.getValue()))
                numberOfMonths = depositTerm;
        else if (depositTermPeriod.equalsIgnoreCase(FDREnum.DEPOSIT_TERM_PERIOD_YEAR.getValue()))
                numberOfMonths = depositTerm * 12;

        return numberOfMonths;
        }

        private Double getYearlyInterestRate(Double interestRate, String interestRateFrequency,
                CalculationMetaProperty calculationMetaProperty) {

        Double yearlyInterestRate = null;
        if (interestRateFrequency.equalsIgnoreCase(FDREnum.INTEREST_RATE_FREQUENCY_YEARLY.getValue()))
                yearlyInterestRate = interestRate;
        if (interestRateFrequency.equalsIgnoreCase(FDREnum.INTEREST_RATE_FREQUENCY_MONTHLY.getValue()))
                yearlyInterestRate = interestRate * 12;

        return yearlyInterestRate;
        }

        private Double getHalfYearlyInterestRate(Double interestRate, String interestRateFrequency,
                CalculationMetaProperty calculationMetaProperty) {

        Double halfYearlyInterestRate = null;
        if (interestRateFrequency.equalsIgnoreCase(FDREnum.INTEREST_RATE_FREQUENCY_YEARLY.getValue()))
                halfYearlyInterestRate = BigDecimal.valueOf(interestRate)
                                .divide(BigDecimal.valueOf(2),
                                                calculationMetaProperty.getInterestRatePrecision(),
                                                CommonFunctions.getRoundingMode(
                                                                calculationMetaProperty.getRoundingLogic()))
                                .doubleValue();
        if (interestRateFrequency.equalsIgnoreCase(FDREnum.INTEREST_RATE_FREQUENCY_MONTHLY.getValue()))
                halfYearlyInterestRate = interestRate * 6;

        return halfYearlyInterestRate;
        }

        private Double getMonthlyInterestRate(Double interestRate, String interestRateFrequency,
                CalculationMetaProperty calculationMetaProperty) {

        Double monthlyInterestRate = null;
        if (interestRateFrequency.equalsIgnoreCase(FDREnum.INTEREST_RATE_FREQUENCY_YEARLY.getValue()))
                monthlyInterestRate = BigDecimal.valueOf(interestRate)
                                .divide(BigDecimal.valueOf(12),
                                                calculationMetaProperty.getInterestRatePrecision(),
                                                CommonFunctions.getRoundingMode(
                                                                calculationMetaProperty.getRoundingLogic()))
                                .doubleValue();
        if (interestRateFrequency.equalsIgnoreCase(FDREnum.INTEREST_RATE_FREQUENCY_MONTHLY.getValue()))
                monthlyInterestRate = interestRate;

        return monthlyInterestRate;
        }

        private Double getQuarterlyInterestRate(Double interestRate, String interestRateFrequency,
                CalculationMetaProperty calculationMetaProperty) {

        Double quarterlyInterestRate = null;
        if (interestRateFrequency.equalsIgnoreCase(FDREnum.INTEREST_RATE_FREQUENCY_YEARLY.getValue()))
                quarterlyInterestRate = BigDecimal.valueOf(interestRate)
                                .divide(BigDecimal.valueOf(4),
                                                calculationMetaProperty.getInterestRatePrecision(),
                                                CommonFunctions.getRoundingMode(
                                                                calculationMetaProperty.getRoundingLogic()))
                                .doubleValue();
        if (interestRateFrequency.equalsIgnoreCase(FDREnum.INTEREST_RATE_FREQUENCY_MONTHLY.getValue()))
                quarterlyInterestRate = interestRate * 3;

        return quarterlyInterestRate;
        }
}
