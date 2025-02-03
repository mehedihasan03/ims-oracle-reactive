package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.SMSNotificationMetaProperty;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.core.util.validation.CommonValidation;
import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.StagingCollectionDataArchiveAdapter;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.IDataArchiveUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.AccountDataInfo;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionGridResponse;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.EmployeePersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanaccount.application.port.in.LoanAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.LoanAdjustmentPersistenceAdapter;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.*;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out.LoanAdjustmentEditHistoryPort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out.LoanAdjustmentPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentDataEditHistory;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out.LoanWaiverPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler.PassbookRequestDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebatePersistencePort;
import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.ISmsNotificationUseCase;
import net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.dto.SmsNotificationRequestDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MemberInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.MobileInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingData;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.out.persistence.IWithdrawStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.domain.StagingWithdrawData;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum.NO;

@Service
@Slf4j
public class LoanAdjustmentService implements LoanAdjustmentUseCase {

    private final TransactionalOperator rxtx;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final IStagingDataUseCase stagingDataUseCase;
    private final TransactionUseCase transactionUseCase;
    private final PassbookUseCase passbookUseCase;
    private final CommonRepository commonRepository;
    private final LoanAccountUseCase loanAccountUseCase;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final LoanAdjustmentPersistencePort port;
    private final DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort;
    private final ISmsNotificationUseCase smsNotificationUseCase;
    private final StagingCollectionDataArchiveAdapter stagingCollectionDataArchiveAdapter;
    private final Gson gson;
    private final LoanAdjustmentPersistenceAdapter loanAdjustmentPersistenceAdapter;
    private final IDataArchiveUseCase iDataArchiveUseCase;
    private final ModelMapper modelMapper;
    private final CommonValidation commonValidation;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final CollectionStagingDataPersistencePort collectionStagingDataPersistencePort;
    private final IWithdrawStagingDataPersistencePort withdrawStagingDataPersistencePort;
    private final LoanAdjustmentEditHistoryPort historyPort;
    private final EmployeePersistencePort employeePersistencePort;
    private final LoanRebatePersistencePort loanRebatePersistencePort;
    private final LoanWaiverPersistencePort loanWaiverPersistencePort;

    public LoanAdjustmentService(
            // @Qualifier("instituteConnectionFactory") ConnectionFactory connectionFactory,
            TransactionalOperator rxtx,
            ManagementProcessTrackerUseCase managementProcessTrackerUseCase,
            OfficeEventTrackerUseCase officeEventTrackerUseCase,
            SamityEventTrackerUseCase samityEventTrackerUseCase,
            IStagingDataUseCase stagingDataUseCase, TransactionUseCase transactionUseCase,
            PassbookUseCase passbookUseCase, CommonRepository commonRepository,
            LoanAccountUseCase loanAccountUseCase,
            LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase,
            LoanAdjustmentPersistencePort port,
            DayEndProcessTrackerPersistencePort dayEndProcessTrackerPersistencePort,
            ISmsNotificationUseCase smsNotificationUseCase,
            StagingCollectionDataArchiveAdapter stagingCollectionDataArchiveAdapter,
            LoanAdjustmentPersistenceAdapter loanAdjustmentPersistenceAdapter,
            IDataArchiveUseCase iDataArchiveUseCase, ModelMapper modelMapper,
            CommonValidation commonValidation,
            ISavingsAccountUseCase savingsAccountUseCase,
            CollectionStagingDataPersistencePort collectionStagingDataPersistencePort,
            IWithdrawStagingDataPersistencePort withdrawStagingDataPersistencePort,
            LoanAdjustmentEditHistoryPort historyPort, EmployeePersistencePort employeePersistencePort,
            LoanRebatePersistencePort loanRebatePersistencePort,
            LoanWaiverPersistencePort loanWaiverPersistencePort) {
        this.passbookUseCase = passbookUseCase;
        this.commonRepository = commonRepository;
        this.loanAccountUseCase = loanAccountUseCase;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        // ReactiveTransactionManager transactionManager = new
        // R2dbcTransactionManager(connectionFactory);
        // this.rxtx = TransactionalOperator.create(transactionManager);
        this.rxtx = rxtx;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.stagingDataUseCase = stagingDataUseCase;
        this.transactionUseCase = transactionUseCase;
        this.port = port;
        this.dayEndProcessTrackerPersistencePort = dayEndProcessTrackerPersistencePort;
        this.smsNotificationUseCase = smsNotificationUseCase;
        this.stagingCollectionDataArchiveAdapter = stagingCollectionDataArchiveAdapter;
        this.iDataArchiveUseCase = iDataArchiveUseCase;
        this.modelMapper = modelMapper;
        this.commonValidation = commonValidation;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.collectionStagingDataPersistencePort = collectionStagingDataPersistencePort;
        this.withdrawStagingDataPersistencePort = withdrawStagingDataPersistencePort;
        this.historyPort = historyPort;
        this.employeePersistencePort = employeePersistencePort;
        this.loanRebatePersistencePort = loanRebatePersistencePort;
        this.loanWaiverPersistencePort = loanWaiverPersistencePort;
        this.gson = CommonFunctions.buildGson(this);
        this.loanAdjustmentPersistenceAdapter = loanAdjustmentPersistenceAdapter;
    }

    @Override
    public Mono<LoanAdjustmentResponseDTO> createLoanAdjustmentForMember(LoanAdjustmentRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();

        AtomicReference<String> samityEventId = StringUtils.isNotBlank(requestDTO.getProcessId()) ? new AtomicReference<>(requestDTO.getProcessId()) : new AtomicReference<>(UUID.randomUUID().toString());
        return this.validateSavingsAccountStatus(requestDTO)
                .flatMap(isValid -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                        .doOnNext(managementProcess::set))
                .flatMap(this::validateIfDayEndProcessIsStartedForOffice)
                .flatMap(managementProcessTracker -> this.validateIfLoanAdjustmentTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), requestDTO.getSamityId())
                        .map(string -> managementProcessTracker))
                .flatMap(managementProcessTracker -> stagingDataUseCase.getStagingDataByMemberId(requestDTO.getMemberId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Staging Data Found For Member")))
                        .flatMap(stagingData -> stagingDataUseCase.getStagingAccountDataListByMemberId(requestDTO.getMemberId())
                                .doOnNext(stagingAccountData -> stagingAccountData.setSavingsAvailableBalance(stagingAccountData.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : stagingAccountData.getSavingsAvailableBalance()))
                                .flatMap(stagingAccountData -> {
                                    if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId())) {
                                        return this.getSavingsAccountTransactionsAndUpdateAvailableBalance(stagingAccountData);
                                    }
                                    if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId())) {
                                        return collectionStagingDataPersistencePort.getCollectionStagingDataByLoanAccountId(stagingAccountData.getLoanAccountId())
                                                .switchIfEmpty(Mono.just(CollectionStagingData.builder().build()))
                                                .flatMap(collectionStagingData -> {
                                                    List<AdjustedAccount> adjustedAccountList = requestDTO.getData().stream().flatMap(adjustedLoanData -> adjustedLoanData.getAdjustedAccountList().stream()).collect(Collectors.toList());
                                                    return validateLoanAdjustedAmount(adjustedAccountList, collectionStagingData, stagingAccountData);
                                                });
                                    }
                                    return Mono.just(stagingAccountData);
                                })
                                .collectList()
                                .map(stagingAccountDataList -> Tuples.of(stagingData, stagingAccountDataList))))
                .doOnNext(tuple -> log.debug("Staging Data and Staging Account Data For Member: {}",
                        tuple))
                .filter(tuple -> this.verifyLoanAdjustmentRequest(requestDTO, tuple))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Adjustment Data is not Verified")))
                .flatMap(tuple -> samityEventTrackerUseCase.getSamityEventByEventTypeForSamity(
                                managementProcess.get().getManagementProcessId(),
                                requestDTO.getSamityId(), SamityEvents.LOAN_ADJUSTED.getValue())
                        .map(samityEventTracker -> {
                            if (!HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())
                                    && HelperUtil.checkIfNullOrEmpty(requestDTO.getProcessId())) {
                                samityEventId.set(samityEventTracker.getSamityEventTrackerId());
                            }
                            return tuple;
                        }))
                .map(tuple -> this.buildLoanAdjustmentDataFromRequest(requestDTO,
                        managementProcess.get().getManagementProcessId(), samityEventId.get(), "Yes"))
                .doOnNext(loanAdjustmentDataList -> log.info("Loan Adjustment Data List: {}", loanAdjustmentDataList))
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::validateLoanRebateAndLoanWaiver)
                .flatMap(adjustmentData -> {
                    if (!HelperUtil.checkIfNullOrEmpty(requestDTO.getAdjustmentType()) &&
                            (requestDTO.getAdjustmentType().equalsIgnoreCase(CollectionType.REGULAR.getValue()) ||
                                    requestDTO.getAdjustmentType().equalsIgnoreCase(CollectionType.SPECIAL.getValue()))) {
                        return commonValidation.checkIfCollectionDataAndAdjustmentDataNotExistsForLoanAccountId(adjustmentData.getLoanAccountId())
                                .flatMap(adjustment -> {
                                    if (adjustment) {
                                        return Mono.just(adjustmentData);
                                    }
                                    return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection data or adjustment data already found for this loan account"));
                                });
                    } else
                        return Mono.just(adjustmentData);
                })
                .collectList()
                .flatMap(port::createAndSaveLoanAdjustmentData)
                .flatMap(loanAdjustmentDataList -> samityEventTrackerUseCase
                        .getSamityEventByEventTypeForSamity(
                                managementProcess.get().getManagementProcessId(),
                                requestDTO.getSamityId(),
                                SamityEvents.LOAN_ADJUSTED.getValue())
                        .flatMap(samityEventTracker -> {
                            if (HelperUtil.checkIfNullOrEmpty(
                                    samityEventTracker.getSamityEvent())) {
                                return samityEventTrackerUseCase.insertSamityEvent(
                                                managementProcess.get().getManagementProcessId(),
                                                samityEventId.get(),
                                                requestDTO.getOfficeId(),
                                                requestDTO.getSamityId(),
                                                SamityEvents.LOAN_ADJUSTED.getValue(),
                                                requestDTO.getLoginId())
                                        .map(samityEvent -> loanAdjustmentDataList);
                            }
                            return Mono.just(loanAdjustmentDataList);
                        }))
                .as(rxtx::transactional)
                .map(data -> LoanAdjustmentResponseDTO.builder()
                        .userMessage("Loan Adjustment Created Successfully")
                        .build())
                .doOnNext(response -> log.info("Loan Adjustment Response: {}", response))
                .doOnError(throwable -> log.error("Error Creating Loan Adjustment: {}", throwable.getMessage()));
    }

    @Override
    public Mono<LoanAdjustmentResponseDTO> createLoanAdjustmentForRebate(LoanAdjustmentRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        AtomicReference<String> samityEventId = StringUtils.isNotBlank(requestDTO.getProcessId()) ? new AtomicReference<>(requestDTO.getProcessId()) : new AtomicReference<>(UUID.randomUUID().toString());
        return this.validateSavingsAccountStatus(requestDTO)
                .flatMap(isValid -> managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                        .doOnNext(managementProcess::set))
                .flatMap(this::validateIfDayEndProcessIsStartedForOffice)
                .flatMap(managementProcessTracker -> this.validateIfLoanAdjustmentTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), requestDTO.getSamityId())
                        .map(string -> managementProcessTracker))
                .flatMap(managementProcessTracker -> stagingDataUseCase.getStagingDataByMemberId(requestDTO.getMemberId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Staging Data Found For Member")))
                        .flatMap(stagingData -> stagingDataUseCase.getStagingAccountDataListByMemberId(requestDTO.getMemberId())
                                .doOnNext(stagingAccountData -> stagingAccountData.setSavingsAvailableBalance(stagingAccountData.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : stagingAccountData.getSavingsAvailableBalance()))
                                .flatMap(stagingAccountData -> {
                                    if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId())) {
                                        return this.getSavingsAccountTransactionsAndUpdateAvailableBalance(stagingAccountData);
                                    }
                                    if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId())) {
                                        return collectionStagingDataPersistencePort.getCollectionStagingDataByLoanAccountId(stagingAccountData.getLoanAccountId())
                                                .switchIfEmpty(Mono.just(CollectionStagingData.builder().build()))
                                                .flatMap(collectionStagingData -> {
                                                    List<AdjustedAccount> adjustedAccountList = requestDTO.getData().stream().flatMap(adjustedLoanData -> adjustedLoanData.getAdjustedAccountList().stream()).collect(Collectors.toList());
                                                    return validateLoanAdjustedAmount(adjustedAccountList, collectionStagingData, stagingAccountData);
                                                });
                                    }
                                    return Mono.just(stagingAccountData);
                                })
                                .collectList()
                                .map(stagingAccountDataList -> Tuples.of(stagingData, stagingAccountDataList))))
                .doOnNext(tuple -> log.debug("Staging Data and Staging Account Data For Member: {}",
                        tuple))
                .filter(tuple -> this.verifyLoanAdjustmentRequest(requestDTO, tuple))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Adjustment Data is not Verified")))
                .flatMap(tuple -> samityEventTrackerUseCase.getSamityEventByEventTypeForSamity(
                                managementProcess.get().getManagementProcessId(),
                                requestDTO.getSamityId(), SamityEvents.LOAN_ADJUSTED.getValue())
                        .map(samityEventTracker -> {
                            if (!HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())
                                    && HelperUtil.checkIfNullOrEmpty(requestDTO.getProcessId())) {
                                samityEventId.set(samityEventTracker.getSamityEventTrackerId());
                            }
                            return tuple;
                        }))
                .map(tuple -> this.buildLoanAdjustmentDataFromRequest(requestDTO,
                        managementProcess.get().getManagementProcessId(), samityEventId.get(), "Yes"))
                .doOnNext(loanAdjustmentDataList -> log.info("Loan Adjustment Data List: {}", loanAdjustmentDataList)).flatMapMany(Flux::fromIterable)
                .flatMap(adjustmentData -> {
                    if (!HelperUtil.checkIfNullOrEmpty(requestDTO.getAdjustmentType()) &&
                            (requestDTO.getAdjustmentType().equalsIgnoreCase(CollectionType.REGULAR.getValue()) ||
                                    requestDTO.getAdjustmentType().equalsIgnoreCase(CollectionType.SPECIAL.getValue()))) {
                        return commonValidation.checkIfCollectionDataAndAdjustmentDataNotExistsForLoanAccountId(adjustmentData.getLoanAccountId())
                                .flatMap(adjustment -> {
                                    if (adjustment) {
                                        return Mono.just(adjustmentData);
                                    }
                                    return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection data or adjustment data already found for this loan account"));
                                });
                    } else
                        return Mono.just(adjustmentData);
                })
                .collectList()
                .flatMap(port::createAndSaveLoanAdjustmentData)
                .flatMap(loanAdjustmentDataList -> samityEventTrackerUseCase
                        .getSamityEventByEventTypeForSamity(
                                managementProcess.get().getManagementProcessId(),
                                requestDTO.getSamityId(),
                                SamityEvents.LOAN_ADJUSTED.getValue())
                        .flatMap(samityEventTracker -> {
                            if (HelperUtil.checkIfNullOrEmpty(
                                    samityEventTracker.getSamityEvent())) {
                                return samityEventTrackerUseCase.insertSamityEvent(
                                                managementProcess.get().getManagementProcessId(),
                                                samityEventId.get(),
                                                requestDTO.getOfficeId(),
                                                requestDTO.getSamityId(),
                                                SamityEvents.LOAN_ADJUSTED.getValue(),
                                                requestDTO.getLoginId())
                                        .map(samityEvent -> loanAdjustmentDataList);
                            }
                            return Mono.just(loanAdjustmentDataList);
                        }))
                .as(rxtx::transactional)
                .map(data -> LoanAdjustmentResponseDTO.builder()
                        .userMessage("Loan Adjustment Created Successfully")
                        .build())
                .doOnNext(response -> log.info("Loan Adjustment Response: {}", response))
                .doOnError(throwable -> log.error("Error Creating Loan Adjustment: {}", throwable.getMessage()));
    }

    private Mono<StagingAccountData> validateLoanAdjustedAmount(List<AdjustedAccount> adjustedAccountList, CollectionStagingData collectionStagingData, StagingAccountData stagingAccountData) {
        BigDecimal requestedAmount = adjustedAccountList.stream().map(AdjustedAccount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPrincipalRemaining = stagingAccountData.getTotalPrincipalRemaining() == null ? BigDecimal.ZERO : stagingAccountData.getTotalPrincipalRemaining();
        BigDecimal totalServiceChargeRemaining = stagingAccountData.getTotalServiceChargeRemaining() == null ? BigDecimal.ZERO : stagingAccountData.getTotalServiceChargeRemaining();
        BigDecimal collectionAmount = collectionStagingData.getAmount() == null ? BigDecimal.ZERO : collectionStagingData.getAmount();
        BigDecimal totalReceivable = totalPrincipalRemaining.add(totalServiceChargeRemaining).subtract(collectionAmount);
        log.info("Principal Remaining: {}, Service Charge Remaining: {}, Requested Amount: {}, Collection Amount Amount: {}", totalPrincipalRemaining, totalServiceChargeRemaining, requestedAmount, collectionAmount);
        if (requestedAmount.compareTo(totalReceivable) <= 0 && requestedAmount.compareTo(BigDecimal.ZERO) >= 0) {
            return Mono.just(stagingAccountData);
        }
        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Requested Amount can not exceed total Account Outstanding..!!"));
    }

    private Mono<Boolean> validateSavingsAccountStatus(LoanAdjustmentRequestDTO loanAdjustmentRequestDTO) {
        List<AdjustedAccount> adjustedAccountList = loanAdjustmentRequestDTO.getData().stream().flatMap(adjustedLoanData -> adjustedLoanData.getAdjustedAccountList().stream()).toList();

        return Flux.fromIterable(adjustedAccountList)
                .flatMap(adjustedAccount -> isSavingsAccountStatusActive(adjustedAccount.getSavingsAccountId())
                        .map(isActive -> Tuples.of(adjustedAccount.getSavingsAccountId(), isActive)))
                .collectList()
                .flatMap(list -> {
                    List<String> inactiveAccounts = list.stream()
                            .filter(tuple -> !tuple.getT2())
                            .map(Tuple2::getT1)
                            .toList();
                    if (inactiveAccounts.isEmpty()) {
                        return Mono.just(true);
                    } else {
                        String errorMessage = "Savings Account(s) not Active: " + String.join(", ", inactiveAccounts);
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, errorMessage));
                    }
                });
    }

    private Mono<Boolean> isSavingsAccountStatusActive(String savingsAccountId) {
        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId)
                .map(savingsAccountDTO -> savingsAccountDTO.getStatus().equals(Status.STATUS_ACTIVE.getValue()));
    }

    @Override
    public Mono<LoanAdjustmentResponseDTO> updateLoanAdjustmentForMember(LoanAdjustmentRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        AtomicReference<String> samityEventId = StringUtils.isNotBlank(requestDTO.getProcessId()) ? new AtomicReference<>(requestDTO.getProcessId()) : new AtomicReference<>(UUID.randomUUID().toString());
        return commonRepository.getManagementProcessTrackerByProcessID(requestDTO.getManagementProcessId())
                .doOnNext(entity -> log.info("Management Process Tracker Entity: {}", entity))
                .map(entity -> {
                    return modelMapper.map(entity, ManagementProcessTracker.class);
//                    gson.fromJson(entity.toString(), ManagementProcessTracker.class)
                })
                .doOnNext(l -> log.info("Management Process Tracker: {}", l))
                .doOnNext(managementProcess::set)
                .flatMap(this::validateIfDayEndProcessIsStartedForOffice)
                .flatMap(managementProcessTracker -> this.validateIfLoanAdjustmentTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), requestDTO.getSamityId())
                        .map(string -> managementProcessTracker))
                .flatMap(managementProcessTracker -> stagingDataUseCase.getStagingDataByMemberId(requestDTO.getMemberId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Staging Data Found For Member")))
                        .flatMap(stagingData -> stagingDataUseCase.getStagingAccountDataListByMemberId(requestDTO.getMemberId())
                                .doOnNext(s -> log.info("Staging Account Data: {}", s))
                                .doOnNext(stagingAccountData -> stagingAccountData.setSavingsAvailableBalance(stagingAccountData.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : stagingAccountData.getSavingsAvailableBalance()))
                                .flatMap(stagingAccountData -> {
                                    if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId())) {
                                        return this.getSavingsAccountTransactionsAndUpdateAvailableBalance(stagingAccountData);
                                    }
                                    return Mono.just(stagingAccountData);
                                })
                                .collectList()
                                .map(stagingAccountDataList -> Tuples.of(stagingData, stagingAccountDataList))))
                .doOnNext(tuple -> log.info("Staging Data and Staging Account Data For Member for update use case : {}", tuple))
                .filter(tuple -> this.verifyLoanAdjustmentRequest(requestDTO, tuple))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Adjustment Data is not Verified")))
                .flatMap(tuple -> samityEventTrackerUseCase.getSamityEventByEventTypeForSamity(
                                managementProcess.get().getManagementProcessId(),
                                requestDTO.getSamityId(), SamityEvents.LOAN_ADJUSTED.getValue())
                        .map(samityEventTracker -> {
                            if (!HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()) && HelperUtil.checkIfNullOrEmpty(requestDTO.getProcessId())) {
                                samityEventId.set(samityEventTracker.getSamityEventTrackerId());
                            }
                            return tuple;
                        }))
                .flatMap(tuple -> iDataArchiveUseCase.saveLoanAdjustmentIntoHistory(requestDTO.getManagementProcessId(), requestDTO.getProcessId()))
                .doOnNext(str -> log.info("Loan Adjustment Data Archived Successfully for update use case : {}", str))
                .flatMap(str -> loanAdjustmentPersistenceAdapter.deleteAllLoanAdjustmentDataByManagementProcessIdAndLoanAdjustmentProcessId(
                        requestDTO.getManagementProcessId(), requestDTO.getProcessId()))
                .doOnNext(str -> log.info("Loan Adjustment Data Deleted Successfully for update use case : {}", str))
                .map(str -> this.buildLoanAdjustmentDataFromRequest(requestDTO,
                        managementProcess.get().getManagementProcessId(), samityEventId.get(), "No"))
                .doOnNext(loanAdjustmentDataList -> log.info("Loan Adjustment Data List for update use case : {}", loanAdjustmentDataList))
                .flatMap(port::createAndSaveLoanAdjustmentData)
                .flatMap(loanAdjustmentDataList -> samityEventTrackerUseCase
                        .getSamityEventByEventTypeForSamity(
                                managementProcess.get().getManagementProcessId(),
                                requestDTO.getSamityId(),
                                SamityEvents.LOAN_ADJUSTED.getValue())
                        .flatMap(samityEventTracker -> {
                            if (HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())) {
                                return samityEventTrackerUseCase.insertSamityEvent(
                                                managementProcess.get().getManagementProcessId(),
                                                samityEventId.get(),
                                                requestDTO.getOfficeId(),
                                                requestDTO.getSamityId(),
                                                SamityEvents.LOAN_ADJUSTED.getValue(),
                                                requestDTO.getLoginId())
                                        .map(samityEvent -> loanAdjustmentDataList);
                            }
                            return Mono.just(loanAdjustmentDataList);
                        }))
                .as(rxtx::transactional)
                .map(data -> LoanAdjustmentResponseDTO.builder()
                        .userMessage("Loan Adjustment Updated Successfully")
                        .build())
                .doOnNext(response -> log.info("Loan Adjustment Response for update use case : {}", response))
                .doOnError(throwable -> log.error("Error Creating Loan Adjustment for update use case : {}", throwable.getMessage()));
    }

    @Override
    public Mono<LoanAdjustmentResponseDTO> deleteLoanAdjustmentAndSaveToHistoryForMember(LoanAdjustmentRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        AtomicReference<String> samityEventId = StringUtils.isNotBlank(requestDTO.getProcessId()) ? new AtomicReference<>(requestDTO.getProcessId()) : new AtomicReference<>(UUID.randomUUID().toString());
        return commonRepository.getManagementProcessTrackerByProcessID(requestDTO.getManagementProcessId())
                .doOnNext(entity -> log.info("Management Process Tracker Entity: {}", entity))
                .map(entity -> {
                    return modelMapper.map(entity, ManagementProcessTracker.class);
//                    gson.fromJson(entity.toString(), ManagementProcessTracker.class)
                })
                .doOnNext(l -> log.info("Management Process Tracker: {}", l))
                .doOnNext(managementProcess::set)
                .flatMap(this::validateIfDayEndProcessIsStartedForOffice)
                .flatMap(managementProcessTracker -> this.validateIfLoanAdjustmentTransactionIsStillAvailableForSamity(managementProcessTracker.getManagementProcessId(), requestDTO.getSamityId())
                        .map(string -> managementProcessTracker))
                .flatMap(managementProcessTracker -> stagingDataUseCase.getStagingDataByMemberId(requestDTO.getMemberId())
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Staging Data Found For Member")))
                        .flatMap(stagingData -> stagingDataUseCase.getStagingAccountDataListByMemberId(requestDTO.getMemberId())
                                .doOnNext(s -> log.info("Staging Account Data: {}", s))
                                .doOnNext(stagingAccountData -> stagingAccountData.setSavingsAvailableBalance(stagingAccountData.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : stagingAccountData.getSavingsAvailableBalance()))
                                .flatMap(stagingAccountData -> {
                                    if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId())) {
                                        return this.getSavingsAccountTransactionsAndUpdateAvailableBalance(stagingAccountData);
                                    }
                                    return Mono.just(stagingAccountData);
                                })
                                .collectList()
                                .map(stagingAccountDataList -> Tuples.of(stagingData, stagingAccountDataList))))
                .doOnNext(tuple -> log.info("Staging Data and Staging Account Data For Member for delete use case : {}", tuple))
//                .filter(tuple -> this.verifyLoanAdjustmentRequest(requestDTO, tuple))
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Adjustment Data is not Verified")))
                .flatMap(tuple -> samityEventTrackerUseCase.getSamityEventByEventTypeForSamity(
                                managementProcess.get().getManagementProcessId(),
                                requestDTO.getSamityId(), SamityEvents.LOAN_ADJUSTED.getValue())
                        .map(samityEventTracker -> {
                            if (!HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()) && HelperUtil.checkIfNullOrEmpty(requestDTO.getProcessId())) {
                                samityEventId.set(samityEventTracker.getSamityEventTrackerId());
                            }
                            return tuple;
                        }))
                .flatMap(tuple -> iDataArchiveUseCase.saveLoanAdjustmentIntoHistory(requestDTO.getManagementProcessId(), requestDTO.getProcessId()))
                .doOnNext(str -> log.info("Loan Adjustment Data Archived Successfully for delete use case : {}", str))
                .flatMap(str -> loanAdjustmentPersistenceAdapter.deleteAllLoanAdjustmentDataByManagementProcessIdAndLoanAdjustmentProcessId(
                        requestDTO.getManagementProcessId(), requestDTO.getProcessId()))
                .doOnNext(str -> log.info("Loan Adjustment Data Deleted Successfully for delete use case : {}", str))
                .flatMap(deleteMessage -> samityEventTrackerUseCase
                        .getSamityEventByEventTypeForSamity(
                                managementProcess.get().getManagementProcessId(),
                                requestDTO.getSamityId(),
                                SamityEvents.LOAN_ADJUSTED.getValue())
                        .flatMap(samityEventTracker -> {
                            if (HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent())) {
                                return samityEventTrackerUseCase.insertSamityEvent(
                                                managementProcess.get().getManagementProcessId(),
                                                samityEventId.get(),
                                                requestDTO.getOfficeId(),
                                                requestDTO.getSamityId(),
                                                SamityEvents.LOAN_ADJUSTED.getValue(),
                                                requestDTO.getLoginId())
                                        .map(samityEvent -> deleteMessage);
                            }
                            return Mono.just(deleteMessage);
                        }))
                .as(rxtx::transactional)
                .map(message -> LoanAdjustmentResponseDTO.builder()
                        .userMessage(message)
                        .build())
                .doOnSuccess(response -> log.info("Loan Adjustment Response for delete and save to history use case : {}", response))
                .doOnError(throwable -> log.error("Error Creating Loan Adjustment for delete use case : {}", throwable.getMessage()));
    }

    private Mono<ManagementProcessTracker> validateIfDayEndProcessIsStartedForOffice(ManagementProcessTracker managementProcessTracker) {
        return dayEndProcessTrackerPersistencePort.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                .collectList()
                .filter(List::isEmpty)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Running For Office")))
                .map(dayEndProcessTrackers -> managementProcessTracker);
    }

    private Mono<StagingAccountData> getSavingsAccountTransactionsAndUpdateAvailableBalance(StagingAccountData stagingAccountData) {
        return commonRepository.getCollectionStagingDataBySavingsAccountId(stagingAccountData.getSavingsAccountId())
                .switchIfEmpty(Mono.just(CollectionStagingDataEntity.builder()
                        .savingsAccountId(stagingAccountData.getSavingsAccountId())
                        .amount(BigDecimal.ZERO)
                        .build()))
                .collectList()
                .flatMap(collectionStagingDataEntityList -> commonRepository.getStagingWithdrawDataBySavingsAccountId(stagingAccountData.getSavingsAccountId())
                        .switchIfEmpty(Mono.just(StagingWithdrawDataEntity.builder()
                                .savingsAccountId(stagingAccountData.getSavingsAccountId())
                                .amount(BigDecimal.ZERO)
                                .build()))
                        .collectList()
                        .map(stagingWithdrawDataEntityList -> Tuples.of(collectionStagingDataEntityList, stagingWithdrawDataEntityList)))
                .map(tuple -> {
                    BigDecimal collectionAmount = tuple.getT1().stream().map(CollectionStagingDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal withdrawAmount = tuple.getT2().stream().map(StagingWithdrawDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);


                    stagingAccountData.setSavingsAvailableBalance(stagingAccountData.getSavingsAvailableBalance().add(collectionAmount).subtract(withdrawAmount));
//                    stagingAccountData.setSavingsAvailableBalance(stagingAccountData.getSavingsAvailableBalance().subtract(withdrawAmount));
                    return stagingAccountData;
                });
    }

    private Mono<String> validateIfLoanAdjustmentTransactionIsStillAvailableForSamity(String managementProcessId, String samityId) {
        return samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityId)
                .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                .map(SamityEventTracker::getSamityEvent)
//                .filter(samityEvent -> !samityEvent.equals(SamityEvents.CANCELED.getValue()))
                .collectList()
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Transaction is Already Authorized")))
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.stream().noneMatch(samityEvent -> samityEvent.equals(SamityEvents.CANCELED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity is Already Canceled and Cannot Create Transaction")))
                .flatMap(samityEventList -> port.getLoanAdjustmentDataBySamity(samityId)
                        .filter(loanAdjustmentData -> !HelperUtil.checkIfNullOrEmpty(loanAdjustmentData.getStatus()))
                        .collectList())
                .filter(loanAdjustmentDataList -> loanAdjustmentDataList.isEmpty() || loanAdjustmentDataList.stream().noneMatch(data -> data.getStatus().equals(Status.STATUS_APPROVED.getValue()) || data.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Loan Adjustment Data is Already Authorized or Unauthorized")))
                .filter(loanAdjustmentDataList -> loanAdjustmentDataList.isEmpty() || loanAdjustmentDataList.stream().allMatch(data -> data.getIsLocked().equals("No")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity Loan Adjustment Transaction is Already Locked for Authorization for Samity")))
                .map(loanAdjustmentDataList -> managementProcessId);
    }

    @Override
    public Mono<LoanAdjustmentResponseDTO> authorizeLoanAdjustmentForSamity(LoanAdjustmentRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        final String transactionProcessId = !HelperUtil.checkIfNullOrEmpty(requestDTO.getTransactionProcessId())
                ? requestDTO.getTransactionProcessId()
                : UUID.randomUUID().toString();
        final String passbookProcessId = !HelperUtil.checkIfNullOrEmpty(requestDTO.getPassbookProcessId())
                ? requestDTO.getPassbookProcessId()
                : UUID.randomUUID().toString();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> this
                        .validateAndUpdateLoanAdjustmentDataForAuthorization(requestDTO))
                .doOnNext(loanAdjustmentDataList -> log.debug("Loan Adjustment Data List: {}",
                        loanAdjustmentDataList))
                .flatMap(loanAdjustmentDataList -> this.createTransactionForLoanAdjustment(
                        loanAdjustmentDataList,
                        managementProcess.get().getManagementProcessId(), requestDTO.getMfiId(),
                        requestDTO.getOfficeId(), requestDTO.getLoginId(),
                        transactionProcessId, requestDTO.getSamityId()))
                .flatMap(transactionList -> this.createSMSNotificationEntryForLoanAdjustment(transactionList, requestDTO.getSmsNotificationMetaPropertyList()))
                .flatMap(transactionList -> this.createPassbookEntryForLoanAdjustment(transactionList,
                        requestDTO.getLoginId(),
                        managementProcess.get().getManagementProcessId(), passbookProcessId))
                .as(rxtx::transactional)
                .map(data -> LoanAdjustmentResponseDTO.builder()
                        .userMessage("Loan Adjustment Authorization Successful for Samity")
                        .build())
                .doOnNext(response -> log.info("Loan Adjustment Authorization Response: {}", response))
                .doOnError(throwable -> log.error("Error in Loan Adjustment Authorization: {}",
                        throwable.getMessage()));
    }

    private Mono<List<Transaction>> createSMSNotificationEntryForLoanAdjustment(List<Transaction> transactionList, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return Flux.fromIterable(transactionList)
                .filter(transaction -> !HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()))
                .flatMap(transaction -> this.createSMSNotificationRequestForTransaction(transaction, smsNotificationMetaPropertyList))
                .collectList()
                .map(transactions -> transactionList)
            .onErrorResume(throwable -> {
                log.error("Error in Creating SMS Notification Entry for Loan Adjustment: {}", throwable.getMessage());
                return Mono.just(transactionList);
            });
    }

    @Override
    public Mono<LoanAdjustmentOfficeResponseDTO> gridViewOfLoanAdjustmentForOffice(LoanAdjustmentRequestDTO requestDTO) {
        final AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList())
                .flatMap(officeEventList -> {
                    if (officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))) {
                        return stagingDataUseCase.getSamityIdListByOfficeId(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId())
                                .collectList()
                                .flatMap(samityIdlist -> commonRepository.getSamityIdListForManagementProcessByOfficeAndSamityEvent(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId(), SamityEvents.LOAN_ADJUSTED.getValue())
                                        .filter(samityIdlist::contains)
                                        .collectList())
                                .flatMap(samityIdList -> this.buildLoanAdjustmentGridViewSamityResponse(managementProcess.get().getManagementProcessId(), samityIdList, requestDTO.getLimit(), requestDTO.getOffset()));
                    }
                    List<LoanAdjustmentSamityGridViewResponseDTO> emptyList = new ArrayList<>();
                    return Mono.just(emptyList);
                })
                .map(samityObjectList -> LoanAdjustmentOfficeResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcess.get().getOfficeNameEn())
                        .officeNameBn(managementProcess.get().getOfficeNameBn())
                        .businessDate(managementProcess.get().getBusinessDate())
                        .businessDay(managementProcess.get().getBusinessDay())
                        .data(samityObjectList)
                        .totalCount(samityObjectList.size())
                        .build())
                .doOnSuccess(loanAdjustmentGridViewResponseDTO -> log.info("Loan Adjustment Office Grid View Response: {}", loanAdjustmentGridViewResponseDTO))
                .doOnError(throwable -> log.error("Error in Loan Adjustment Office Grid View: {}", throwable.getMessage()));
    }

    private Mono<Transaction> createSMSNotificationRequestForTransaction(Transaction transaction, List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList) {
        return Mono.fromSupplier(() -> smsNotificationMetaPropertyList.stream()
                        .filter(metaProperty -> metaProperty.getType().equalsIgnoreCase(transaction.getTransactionCode()))
                        .findFirst()
                        .get())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SMS Notification Meta Property Not Found for Transaction Type: " + transaction.getTransactionCode())))
                .flatMap(smsNotificationMetaProperty -> {
                    if (smsNotificationMetaProperty.getIsSMSNotificationEnabled().equals("Yes")) {
                        return this.createAndSaveSMSNotificationRequest(transaction, smsNotificationMetaProperty);
                    }
                    log.info("SMS Notification is Disabled for Transaction Type: {}", transaction.getTransactionCode());
                    return Mono.just(transaction);
                });
    }

    private Mono<Transaction> createAndSaveSMSNotificationRequest(Transaction transaction, SMSNotificationMetaProperty smsNotificationMetaProperty) {
        log.info("Creating and Saving SMS Notification Entry for Transaction with AccountId: {}, Transaction Code: {} and transaction Amount: {}", !HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) ? transaction.getLoanAccountId() : transaction.getSavingsAccountId(), transaction.getTransactionCode(), transaction.getAmount());
        return stagingDataUseCase.getStagingDataByAccountId(!HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) ? transaction.getLoanAccountId() : transaction.getSavingsAccountId())
                .flatMap(stagingData -> commonRepository.getInstituteOidByMFIId(transaction.getMfiId())
                        .map(instituteOid -> Tuples.of(stagingData, instituteOid)))
                .map(tuples -> {
                    StagingData stagingData = tuples.getT1();
                    MobileInfoDTO mobileInfoDTO = gson.fromJson(gson.fromJson(stagingData.getMobile(), ArrayList.class)
                            .get(0)
                            .toString(), MobileInfoDTO.class);
                    return SmsNotificationRequestDTO.builder()
                            .type(transaction.getTransactionCode())
                            .id(transaction.getTransactionId())
                            .amount(String.valueOf(transaction.getAmount()))
                            .datetime(String.valueOf(transaction.getTransactionDate()))
                            .accountId(!HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) ? transaction.getLoanAccountId() : transaction.getSavingsAccountId())
                            .memberId(stagingData.getMemberId())
                            .mobileNumber(mobileInfoDTO.getContactNo())
                            .template(smsNotificationMetaProperty.getTemplate())
                            .mfiId(transaction.getMfiId())
                            .instituteOid(tuples.getT2())
                            .build();
                })
                .doOnNext(smsNotificationRequestDTO -> log.info("SMS Notification Entry Request for Account: {}, Mobile: {} and Amount: {}", smsNotificationRequestDTO.getAccountId(), smsNotificationRequestDTO.getMobileNumber(), smsNotificationRequestDTO.getAmount()))
                .flatMap(smsNotificationUseCase::saveSmsLog)
                .doOnNext(smsLog -> log.info("SMS Notification Entry Saved for Mobile No: {}, with SMS Body: {}", smsLog.getMobileNo(), smsLog.getSms()))
                .map(smsLog -> transaction)
                .onErrorResume(throwable -> {
//                    log.error("Error in Creating and Saving SMS Notification Entry: {}", throwable.getMessage());
                    return Mono.just(transaction);
                });
    }

    private Mono<List<LoanAdjustmentSamityGridViewResponseDTO>> buildLoanAdjustmentGridViewSamityResponse(String managementProcessId, List<String> samityIdList, Integer limit, Integer offset) {
        log.info("Loan Adjustment Samity Id List: {}", samityIdList);
        List<String> paginatedSamityIdList = samityIdList.stream().skip((long) limit * offset).limit(limit).toList();
        log.info("Paginated Loan Adjustment Samity Id List: {}", paginatedSamityIdList);
        return stagingDataUseCase.getStagingProcessTrackerListBySamityIdList(managementProcessId, paginatedSamityIdList)
                .map(stagingProcessTrackerList -> stagingProcessTrackerList.stream()
                        .map(stagingProcessTracker -> gson.fromJson(stagingProcessTracker.toString(), LoanAdjustmentSamityGridViewResponseDTO.class))
                        .toList())
                .flatMapIterable(samityObjectList -> samityObjectList)
                .flatMap(samityObject -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessId, samityObject.getSamityId())
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .map(SamityEventTracker::getSamityEvent)
                        .collectList()
                        .map(samityEventList -> {
                            if (samityEventList.isEmpty()) {
                                samityObject.setStatus("Loan Adjustment Incomplete");
                                samityObject.setBtnViewEnabled("No");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                            } else {
                                if (samityEventList.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.CANCELED.getValue()))) {
                                    samityObject.setStatus("Samity Canceled");
                                    samityObject.setBtnViewEnabled("No");
                                    samityObject.setBtnEditEnabled("No");
                                    samityObject.setBtnSubmitEnabled("No");
                                } else if (samityEventList.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.LOAN_ADJUSTED.getValue()))) {
                                    if (samityEventList.stream().anyMatch(samityEvent -> samityEvent.equals(SamityEvents.AUTHORIZED.getValue()))) {
                                        samityObject.setStatus("Loan Adjustment Authorized");
                                        samityObject.setBtnViewEnabled("Yes");
                                        samityObject.setBtnEditEnabled("No");
                                        samityObject.setBtnSubmitEnabled("No");
                                    } else {
                                        samityObject.setStatus("Loan Adjustment Completed");
                                        samityObject.setBtnViewEnabled("Yes");
                                        samityObject.setBtnEditEnabled("Yes");
                                        samityObject.setBtnSubmitEnabled("Yes");
                                    }
                                } else {
                                    samityObject.setStatus("Loan Adjustment Incomplete");
                                    samityObject.setBtnViewEnabled("No");
                                    samityObject.setBtnEditEnabled("No");
                                    samityObject.setBtnSubmitEnabled("No");
                                }
                            }

                            if (samityObject.getTotalMember() == null || samityObject.getTotalMember() == 0) {
                                samityObject.setStatus("Loan Adjustment Unavailable");
                                samityObject.setBtnViewEnabled("No");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                                samityObject.setRemarks("No Member in Samity");
                            }
                            return samityObject;
                        }))
                .flatMap(samityObject -> this.getLoanAdjustedAmountAndBuildSamityDataResponse(managementProcessId, samityObject))
                .sort(Comparator.comparing(LoanAdjustmentSamityGridViewResponseDTO::getSamityId))
                .collectList();
    }

    private Mono<LoanAdjustmentSamityGridViewResponseDTO> getLoanAdjustedAmountAndBuildSamityDataResponse(String managementProcessId, LoanAdjustmentSamityGridViewResponseDTO samityObject) {
        return port.getLoanAdjustmentDataBySamity(samityObject.getSamityId())
                .filter(loanAdjustmentData -> !HelperUtil.checkIfNullOrEmpty(loanAdjustmentData.getLoanAccountId()))
                .collectList()
                .doOnNext(loanAdjustmentDataList -> log.info("Samity Id: {} Loan Adjustment Data List size: {}", samityObject.getSamityId(), loanAdjustmentDataList.size()))
                .map(loanAdjustmentDataList -> {
                    if (!loanAdjustmentDataList.isEmpty()) {
                        if (samityObject.getStatus().equals("Loan Adjustment Completed")) {
                            if (loanAdjustmentDataList.stream().allMatch(withdrawData -> withdrawData.getStatus().equals(Status.STATUS_UNAUTHORIZED.getValue()))) {
                                samityObject.setStatus("Loan Adjustment Unauthorized");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                            } else if (loanAdjustmentDataList.stream().allMatch(withdrawData -> withdrawData.getStatus().equals(Status.STATUS_REJECTED.getValue()))) {
                                if (loanAdjustmentDataList.stream().allMatch(withdrawData -> withdrawData.getIsSubmitted().equals("Yes"))) {
                                    samityObject.setStatus("Loan Adjustment Submitted");
                                    samityObject.setBtnEditEnabled("No");
                                    samityObject.setBtnSubmitEnabled("No");
                                } else {
                                    samityObject.setStatus("Loan Adjustment Rejected");
                                    samityObject.setBtnEditEnabled("Yes");
                                    samityObject.setBtnSubmitEnabled("Yes");
                                }
                            } else if (loanAdjustmentDataList.stream().allMatch(withdrawData -> withdrawData.getIsLocked().equals("Yes"))) {
                                samityObject.setStatus("Loan Adjustment Locked");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                            } else if (loanAdjustmentDataList.stream().allMatch(withdrawData -> withdrawData.getIsSubmitted().equals("Yes"))) {
                                samityObject.setStatus("Loan Adjustment Submitted");
                                samityObject.setBtnEditEnabled("No");
                                samityObject.setBtnSubmitEnabled("No");
                            }
                        }

                        BigDecimal totalLoanAdjustment = loanAdjustmentDataList.stream()
                                .filter(loanAdjustmentData -> !HelperUtil.checkIfNullOrEmpty(loanAdjustmentData.getLoanAccountId()))
                                .map(LoanAdjustmentData::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        samityObject.setTotalLoanAdjustment(totalLoanAdjustment);
                    } else {
                        samityObject.setTotalLoanAdjustment(BigDecimal.ZERO);
                    }
                    return samityObject;
                });
    }

    private Mono<LoanAdjustmentOfficeResponseDTO> getGridViewOfLoanAdjustmentResponse(LoanAdjustmentRequestDTO requestDTO) {
        final AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .map(managementProcessTracker -> LoanAdjustmentOfficeResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcessTracker.getOfficeNameEn())
                        .officeNameBn(managementProcessTracker.getOfficeNameBn())
                        .businessDate(managementProcessTracker.getBusinessDate())
                        .businessDay(managementProcessTracker.getBusinessDay())
                        .build())
                .flatMap(responseDTO -> this.getSamityResponseForOffice(
                                managementProcess.get().getManagementProcessId(),
                                requestDTO.getOfficeId(), requestDTO.getLimit(), requestDTO.getOffset())
                        .map(samityResponseList -> {
                            responseDTO.setData(samityResponseList);
                            responseDTO.setTotalCount(samityResponseList.size());
                            return responseDTO;
                        }));
    }

    @Override
    public Mono<LoanAdjustmentFieldOfficerResponseDTO> gridViewOfLoanAdjustmentForFieldOfficer(LoanAdjustmentRequestDTO requestDTO) {
        final AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList())
                .flatMap(officeEventList -> {
                    if (officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))) {
                        return stagingDataUseCase.getSamityIdListByFieldOfficer(requestDTO.getFieldOfficerId())
                                .collectList()
                                .flatMap(samityIdlist -> commonRepository.getSamityIdListForManagementProcessByOfficeAndSamityEvent(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId(), SamityEvents.LOAN_ADJUSTED.getValue())
                                        .filter(samityIdlist::contains)
                                        .collectList())
                                .flatMap(samityIdList -> this.buildLoanAdjustmentGridViewSamityResponse(managementProcess.get().getManagementProcessId(), samityIdList, requestDTO.getLimit(), requestDTO.getOffset()));
                    }
                    List<LoanAdjustmentSamityGridViewResponseDTO> emptyList = new ArrayList<>();
                    return Mono.just(emptyList);
                })
                .map(samityObjectList -> LoanAdjustmentFieldOfficerResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcess.get().getOfficeNameEn())
                        .officeNameBn(managementProcess.get().getOfficeNameBn())
                        .businessDate(managementProcess.get().getBusinessDate())
                        .businessDay(managementProcess.get().getBusinessDay())
                        .fieldOfficerId(requestDTO.getFieldOfficerId())
                        .data(samityObjectList)
                        .totalCount(samityObjectList.size())
                        .build())
                .flatMap(responseDTO -> {
                    if (responseDTO.getData().isEmpty()) {
                        return commonRepository.getFieldOfficerByFieldOfficerId(requestDTO.getFieldOfficerId())
                                .map(fieldOfficerEntity -> {
                                    responseDTO.setFieldOfficerNameEn(fieldOfficerEntity.getFieldOfficerNameEn());
                                    responseDTO.setFieldOfficerNameBn(fieldOfficerEntity.getFieldOfficerNameBn());
                                    return responseDTO;
                                });
                    }
                    responseDTO.setFieldOfficerNameEn(responseDTO.getData().get(0).getFieldOfficerNameEn());
                    responseDTO.setFieldOfficerNameBn(responseDTO.getData().get(0).getFieldOfficerNameBn());
                    return Mono.just(responseDTO);
                })
                .doOnSuccess(loanAdjustmentGridViewResponseDTO -> log.info("Loan Adjustment by Field Officer Grid View Response: {}", loanAdjustmentGridViewResponseDTO))
                .doOnError(throwable -> log.error("Error in Loan Adjustment Field Officer Grid View: {}", throwable.getMessage()));
    }

    private Mono<List<LoanAdjustmentSamityGridViewResponseDTO>> getSamityResponseForOffice(
            String managementProcessId, String officeId, Integer limit, Integer offset) {
        return stagingDataUseCase.getStagingProcessEntityByOffice(managementProcessId, officeId)
                .map(stagingProcessTrackerEntity -> gson.fromJson(
                        stagingProcessTrackerEntity.toString(),
                        LoanAdjustmentSamityGridViewResponseDTO.class))
                .collectList()
                .map(samityResponseList -> samityResponseList.stream()
                        .sorted(Comparator.comparing(
                                LoanAdjustmentSamityGridViewResponseDTO::getSamityId))
                        .skip((long) limit * offset).limit(limit).toList())
                .flatMap(samityResponseList -> {
                    if (!samityResponseList.isEmpty()) {
                        return this.getTotalLoanAdjustmentAndSetBtnStatusForSamityResponse(
                                        samityResponseList)
                                .map(responseList -> responseList.stream()
                                        .sorted(Comparator.comparing(
                                                LoanAdjustmentSamityGridViewResponseDTO::getSamityId))
                                        .toList());
                    }
                    return Mono.just(new ArrayList<>());
                });
    }

    private Mono<List<LoanAdjustmentSamityGridViewResponseDTO>> getTotalLoanAdjustmentAndSetBtnStatusForSamityResponse(
            List<LoanAdjustmentSamityGridViewResponseDTO> samityResponseList) {
        List<String> samityIdList = samityResponseList.stream()
                .map(LoanAdjustmentSamityGridViewResponseDTO::getSamityId).toList();
        return port.getAllLoanAdjustmentDataBySamityIdList(samityIdList)
                .map(loanAdjustmentDataList -> {
                    if (!loanAdjustmentDataList.isEmpty()) {
                        samityResponseList.forEach(responseDTO -> {
                            List<LoanAdjustmentData> samityLoanAdjustmentDataList = loanAdjustmentDataList
                                    .stream()
                                    .filter(loanAdjustmentData -> loanAdjustmentData
                                            .getSamityId()
                                            .equals(responseDTO
                                                    .getSamityId()))
                                    .toList();
                            if (!samityLoanAdjustmentDataList.isEmpty()) {
                                BigDecimal totalLoanAdjustment = samityLoanAdjustmentDataList
                                        .stream()
                                        .filter(loanAdjustmentData -> !HelperUtil
                                                .checkIfNullOrEmpty(
                                                        loanAdjustmentData
                                                                .getLoanAccountId()))
                                        .map(LoanAdjustmentData::getAmount)
                                        .reduce(BigDecimal.ZERO,
                                                BigDecimal::add);
                                responseDTO.setTotalLoanAdjustment(totalLoanAdjustment);
                                responseDTO.setStatus(this.getSamityResponseStatus(
                                        samityLoanAdjustmentDataList));
                            } else {
                                responseDTO.setTotalLoanAdjustment(BigDecimal.ZERO);
                                responseDTO.setStatus("Loan Adjustment Incomplete");
                            }
                        });
                    } else {
                        samityResponseList.forEach(responseDTO -> {
                            responseDTO.setTotalLoanAdjustment(BigDecimal.ZERO);
                            responseDTO.setStatus("Loan Adjustment Incomplete");
                        });
                    }
                    samityResponseList.forEach(responseDTO -> {
                        if (responseDTO.getStatus().equals("Loan Adjustment Incomplete")
                                || responseDTO.getStatus()
                                .equals("Loan Adjustment Submitted")
                                || responseDTO.getStatus()
                                .equals("Loan Adjustment Authorized")) {
                            responseDTO.setBtnSubmitEnabled("No");
                        } else {
                            responseDTO.setBtnSubmitEnabled("Yes");
                        }
                    });
                    return samityResponseList;
                });
    }

    private String getSamityResponseStatus(List<LoanAdjustmentData> samityLoanAdjustmentDataList) {
        if (samityLoanAdjustmentDataList.stream().map(LoanAdjustmentData::getStatus)
                .anyMatch(status -> status.equals(Status.STATUS_STAGED.getValue())
                        || status.equals(Status.STATUS_REJECTED.getValue()))) {
            return "Loan Adjustment Completed";
        } else if (samityLoanAdjustmentDataList.stream().map(LoanAdjustmentData::getStatus)
                .allMatch(status -> status.equals(Status.STATUS_SUBMITTED.getValue())
                        || status.equals(Status.STATUS_UNAUTHORIZED.getValue()))) {
            return "Loan Adjustment Submitted";
        } else if (samityLoanAdjustmentDataList.stream().map(LoanAdjustmentData::getStatus)
                .allMatch(status -> status.equals(Status.STATUS_APPROVED.getValue()))) {
            return "Loan Adjustment Authorized";
        }
        return "Loan Adjustment Incomplete";
    }

    private Mono<LoanAdjustmentOfficeResponseDTO> getSamityResponseForOffice(
            LoanAdjustmentOfficeResponseDTO responseDTO) {
        AtomicReference<String> fieldOfficerId = new AtomicReference<>();
        return commonRepository.getSamityByOfficeId(responseDTO.getOfficeId())
                .doOnNext(samity -> fieldOfficerId.set(samity.getFieldOfficerId()))
                .doOnNext(samity -> log.info("Field Officer Id: {}", fieldOfficerId.get()))
                .map(Samity::getSamityId)
                .flatMap(samityId -> this
                        .detailViewOfLoanAdjustmentForSamity(LoanAdjustmentRequestDTO.builder()
                                .samityId(samityId)
                                .build()))
                // .doOnNext(loanAdjustmentSamityGridViewResponseDTO ->
                // log.info("loanAdjustmentSamityGridViewResponseDTO: {}",
                // loanAdjustmentSamityGridViewResponseDTO))
                .flatMap(loanAdjustmentSamityGridViewResponseDTO -> commonRepository
                        .getFieldOfficerByFieldOfficerId(fieldOfficerId.get())
                        .map(fieldOfficerEntity -> {
                            loanAdjustmentSamityGridViewResponseDTO.setFieldOfficerId(
                                    fieldOfficerEntity.getFieldOfficerId());
                            loanAdjustmentSamityGridViewResponseDTO.setFieldOfficerNameEn(
                                    fieldOfficerEntity.getFieldOfficerNameEn());
                            loanAdjustmentSamityGridViewResponseDTO.setFieldOfficerNameBn(
                                    fieldOfficerEntity.getFieldOfficerNameBn());
                            return loanAdjustmentSamityGridViewResponseDTO;
                        }))
                .map(samityResponse -> {
                    samityResponse.setData(null);
                    samityResponse.setTotalCount(null);
                    return samityResponse;
                })
                .sort(Comparator.comparing(LoanAdjustmentSamityGridViewResponseDTO::getSamityId))
                .collectList()
                .map(samityResponseList -> {
                    responseDTO.setData(samityResponseList);
                    responseDTO.setTotalCount(samityResponseList.size());
                    return responseDTO;
                });
    }

    @Override
    public Mono<LoanAdjustmentSamityGridViewResponseDTO> detailViewOfLoanAdjustmentForSamity(
            LoanAdjustmentRequestDTO requestDTO) {

        return port.getLoanAdjustmentDataBySamity(requestDTO.getSamityId())
                .collectList()
                .doOnNext(list -> log.debug("Loan Adjustment Data List: {}", list))
                .flatMap(loanAdjustmentDataList -> {
                    if (loanAdjustmentDataList.isEmpty()) {
                        return Mono.just(
                                new ArrayList<LoanAdjustmentMemberGridViewResponseDTO>());
                    }
                    return this.buildAdjustedLoanAccountListForSamity(loanAdjustmentDataList);
                })
                .doOnNext(loanAdjustmentMemberGridViewResponseList -> log.info(
                        "Loan Adjustment Member Response List: {}",
                        loanAdjustmentMemberGridViewResponseList))
                .flatMap(loanAdjustmentMemberResponseList -> commonRepository
                        .getSamityBySamityId(requestDTO.getSamityId())
                        .map(samity -> LoanAdjustmentSamityGridViewResponseDTO.builder()
                                .samityId(requestDTO.getSamityId())
                                .samityNameEn(samity.getSamityNameEn())
                                .samityNameBn(samity.getSamityNameBn())
                                .data(loanAdjustmentMemberResponseList)
                                .totalCount(loanAdjustmentMemberResponseList.size())
                                .build()))
                .map(this::setBtnStatusForLoanAdjustmentGridViewBySamity)
                .doOnNext(loanAdjustmentGridViewResponseDTO -> log.info(
                        "Loan Adjustment Samity Grid View Response: {}",
                        loanAdjustmentGridViewResponseDTO))
                .doOnError(throwable -> log.error("Error in Loan Adjustment Samity Grid View: {}",
                        throwable.getMessage()));
    }

    private LoanAdjustmentSamityGridViewResponseDTO setBtnStatusForLoanAdjustmentGridViewBySamity(
            LoanAdjustmentSamityGridViewResponseDTO loanAdjustmentSamityGridViewResponseDTO) {
        List<String> statusList = new ArrayList<>();
        loanAdjustmentSamityGridViewResponseDTO.getData().forEach(memberResponse -> {
            statusList.addAll(memberResponse.getAdjustedLoanAccountList().stream()
                    .map(AdjustedLoanAccount::getStatus).distinct().toList());
        });
        String responseStatus = null;
        if (statusList.stream().allMatch(status -> status.equals(Status.STATUS_APPROVED.getValue()))) {
            responseStatus = "Authorized";
        } else if (statusList.stream().anyMatch(status -> status.equals(Status.STATUS_STAGED.getValue()))) {
            responseStatus = "Completed";
        } else if (statusList.stream().allMatch(status -> status.equals(Status.STATUS_SUBMITTED.getValue()))) {
            responseStatus = "Submitted";
        } else if (statusList.stream()
                .allMatch(status -> status.equals(Status.STATUS_UNAUTHORIZED.getValue()))) {
            responseStatus = "Unauthorized";
        }

        if (statusList.isEmpty()) {
            loanAdjustmentSamityGridViewResponseDTO.setStatus("Loan Adjustment Incomplete");
        } else {
            loanAdjustmentSamityGridViewResponseDTO.setStatus("Loan Adjustment " + responseStatus);
        }
        return loanAdjustmentSamityGridViewResponseDTO;
    }

    private Mono<List<LoanAdjustmentMemberGridViewResponseDTO>> buildAdjustedLoanAccountListForSamity(
            List<LoanAdjustmentData> loanAdjustmentDataList) {
        List<String> memberIdList = loanAdjustmentDataList.stream().map(LoanAdjustmentData::getMemberId)
                .distinct().toList();
        log.info("Member Id List for Loan Adjustment: {}", memberIdList);
        return Flux.fromIterable(memberIdList)
                .flatMap(memberId -> {
                    List<LoanAdjustmentData> adjustmentDataList = loanAdjustmentDataList.stream()
                            .filter(loanAdjustmentData -> loanAdjustmentData.getMemberId()
                                    .equals(memberId))
                            .toList();
                    List<AdjustedLoanAccount> adjustedLoanAccountList = this
                            .buildAdjustmentLoanAccountForAMember(adjustmentDataList);
                    log.info("Adjusted Loan Account List For Member: {} is: {}", memberId,
                            adjustedLoanAccountList);
                    return commonRepository.getMemberEntityByMemberId(memberId)
                            .map(memberEntity -> LoanAdjustmentMemberGridViewResponseDTO
                                    .builder()
                                    .memberId(memberId)
                                    .memberNameEn(memberEntity.getMemberNameEn())
                                    .memberNameBn(memberEntity.getMemberNameBn())
                                    .mobile(this.extractMobileNumberFromMobileDetails(memberEntity.getMobile()))
                                    .registerBookSerialId(memberEntity.getRegisterBookSerialId())
                                    .gender(memberEntity.getGender())
                                    .maritalStatus(memberEntity.getMaritalStatus())
                                    .fatherNameEn(memberEntity.getFatherNameEn())
                                    .fatherNameBn(memberEntity.getFatherNameBn())
                                    .spouseNameEn(memberEntity.getSpouseNameEn())
                                    .spouseNameBn(memberEntity.getSpouseNameBn())
                                    .adjustedLoanAccountList(
                                            adjustedLoanAccountList)
                                    .totalCount(adjustedLoanAccountList.size())
                                    .build());
                })
                .collectList();
    }

    private String extractMobileNumberFromMobileDetails(String mobileNoArray) {
        ArrayList mobileList = gson.fromJson(mobileNoArray, ArrayList.class);
        MobileInfoDTO mobileInfoDTO = new MobileInfoDTO();
        if (!mobileList.isEmpty()) {
            try {
                mobileInfoDTO = gson.fromJson(mobileList.get(0).toString(), MobileInfoDTO.class);
            } catch (Exception e) {
                log.error("Error in parsing mobile info: {}", e.getMessage());
                mobileInfoDTO = new MobileInfoDTO();
            }
        }
        return mobileInfoDTO.getContactNo() != null ? mobileInfoDTO.getContactNo() : "";
    }

    private Mono<List<Passbook>> createPassbookEntryForLoanAdjustment(List<Transaction> transactionList,
                                                                      String loginId, String managementProcessId, String passbookProcessId) {
        return this.createPassbookEntryForSavingsAccountsForLoanAdjustment(transactionList, loginId,
                        managementProcessId, passbookProcessId)
                .flatMap(passbookList -> this.createPassbookEntryForLoanAccountsForLoanAdjustment(
                        transactionList, loginId, managementProcessId, passbookProcessId));
    }

    private Mono<List<Passbook>> createPassbookEntryForSavingsAccountsForLoanAdjustment(List<Transaction> transactionList, String loginId, String managementProcessId, String passbookProcessId) {
        List<String> savingsAccountIdList = transactionList.stream()
                .map(Transaction::getSavingsAccountId)
                .filter(savingsAccountId -> !HelperUtil.checkIfNullOrEmpty(savingsAccountId))
                .distinct()
                .toList();
        // log.info("Savings Account Id List: {}", savingsAccountIdList);

        return Flux.fromIterable(transactionList)
                .filter(transaction -> !HelperUtil.checkIfNullOrEmpty(transaction.getSavingsAccountId()) && savingsAccountIdList.contains(transaction.getSavingsAccountId()))
                .flatMap(transaction -> passbookUseCase.createPassbookEntryForSavingsWithdraw(PassbookRequestDTO.builder()
                        .managementProcessId(transaction.getManagementProcessId())
                        .processId(passbookProcessId)
                        .savingsAccountId(transaction.getSavingsAccountId())
                        .amount(transaction.getAmount())
                        .transactionId(transaction.getTransactionId())
                        .transactionCode(transaction.getTransactionCode())
                        .mfiId(transaction.getMfiId())
                        .loginId(loginId)
                        .transactionDate(transaction.getTransactionDate())
                        .paymentMode(transaction.getPaymentMode())
                        .officeId(transaction.getOfficeId())
                        .samityId(transaction.getSamityId())
                        .build()))
                .map(passbookResponseDTO -> gson.fromJson(passbookResponseDTO.toString(), Passbook.class))
                .collectList()
                .doOnNext(passbookList -> log.info("Passbook Savings Accounts Entry For Loan Adjustment: {}", passbookList));
    }

    private Mono<List<Passbook>> createPassbookEntryForSavingsAccountsForLoanAdjustmentFromStagingData(
            List<Transaction> transactionList, String loginId, String managementProcessId,
            String passbookProcessId) {
        List<String> savingsAccountIdList = transactionList.stream()
                .map(Transaction::getSavingsAccountId)
                .filter(savingsAccountId -> !HelperUtil.checkIfNullOrEmpty(savingsAccountId))
                .distinct()
                .toList();
        // log.info("Savings Account Id List: {}", savingsAccountIdList);
        return stagingDataUseCase.getStagingAccountDataBySavingsAccountIdList(savingsAccountIdList)
                .flatMap(stagingAccountData -> commonRepository
                        .getSavingsAccountOidForSavingsAccount(
                                stagingAccountData.getSavingsAccountId())
                        .map(savingsAccountOid -> Tuples.of(savingsAccountOid,
                                stagingAccountData)))
                .map(tuple -> {
                    final StagingAccountData stagingAccountData = tuple.getT2();
                    final String savingsAccountOid = tuple.getT1();
                    AtomicReference<BigDecimal> savingsAccountBeginBalance = new AtomicReference<>(
                            stagingAccountData.getBalance());
                    AtomicReference<BigDecimal> currentAvailableBalance = new AtomicReference<>(
                            stagingAccountData.getSavingsAvailableBalance());
                    AtomicReference<BigDecimal> savingsAccountEndingBalance = new AtomicReference<>(
                            stagingAccountData.getBalance());
                    AtomicReference<BigDecimal> totalWithdrawAmount = new AtomicReference<>(
                            stagingAccountData.getTotalWithdraw());

                    List<Passbook> passbookList = new ArrayList<>();

                    transactionList.forEach(transaction -> {
                        if (!HelperUtil.checkIfNullOrEmpty(transaction.getSavingsAccountId())
                                && transaction.getSavingsAccountId()
                                .equals(stagingAccountData
                                        .getSavingsAccountId())) {
                            currentAvailableBalance.set(currentAvailableBalance.get()
                                    .subtract(transaction.getAmount()));
                            savingsAccountEndingBalance.set(savingsAccountEndingBalance
                                    .get().subtract(transaction.getAmount()));
                            totalWithdrawAmount.set(totalWithdrawAmount.get()
                                    .add(transaction.getAmount()));
                            passbookList.add(Passbook.builder()
                                    .transactionId(transaction.getTransactionId())
                                    .managementProcessId(managementProcessId)
                                    .processId(passbookProcessId)
                                    .loanAdjustmentProcessId(transaction
                                            .getLoanAdjustmentProcessId())
                                    .memberId(transaction.getMemberId())
                                    .savingsAccountId(transaction
                                            .getSavingsAccountId())
                                    .withdrawAmount(transaction.getAmount())
                                    .savgAcctBeginBalance(savingsAccountBeginBalance
                                            .get())
                                    .savgAcctEndingBalance(
                                            savingsAccountEndingBalance
                                                    .get())
                                    .savingsAvailableBalance(
                                            currentAvailableBalance.get())
                                    .totalWithdrawAmount(totalWithdrawAmount.get())
                                    .transactionDate(transaction
                                            .getTransactionDate())
                                    .transactionCode(transaction
                                            .getTransactionCode())
                                    .paymentMode(transaction.getPaymentMode())
                                    .mfiId(transaction.getMfiId())
                                    .status(Status.STATUS_ACTIVE.getValue())
                                    .createdBy(loginId)
                                    .createdOn(LocalDateTime.now())
                                    .savingsAccountOid(savingsAccountOid)
                                    .build());
                            savingsAccountBeginBalance.set(savingsAccountBeginBalance.get()
                                    .subtract(transaction.getAmount()));
                        }
                    });
                    return passbookList;
                })
                .flatMapIterable(passbooks -> passbooks)
                .collectList()
                .flatMap(passbookUseCase::createPassbookEntryForSavingsAccountForLoanAdjustment)
                .doOnNext(passbookList -> log.info(
                        "Passbook Savings Accounts Entry For Loan Adjustment: {}",
                        passbookList));
    }

    private Mono<List<Passbook>> createPassbookEntryForLoanAccountsForLoanAdjustment(
            List<Transaction> transactionList, String loginId, String managementProcessId,
            String passbookProcessId) {
        return Flux.fromIterable(transactionList)
                .filter(transaction -> !HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()))
                .map(transaction -> PassbookRequestDTO.builder()
                        .managementProcessId(managementProcessId)
                        .processId(passbookProcessId)
                        .officeId(transaction.getOfficeId())
                        .loanAccountId(transaction.getLoanAccountId())
                        .amount(transaction.getAmount())
                        .transactionId(transaction.getTransactionId())
                        .transactionCode(transaction.getTransactionCode())
                        .transactionDate(transaction.getTransactionDate())
                        .mfiId(transaction.getMfiId())
                        .paymentMode(transaction.getPaymentMode())
                        .loginId(loginId)
                        .loanAdjustmentProcessId(transaction.getLoanAdjustmentProcessId())
                        .samityId(transaction.getSamityId())
                        .build())
                .flatMap(passbookUseCase::getRepaymentScheduleAndCreatePassbookEntryForLoan)
                .flatMap(passbookResponseDTOList1 -> updateRepaymentScheduleForLoanAdjustmentPaidInstallment(passbookResponseDTOList1, managementProcessId))
                .flatMapIterable(lists -> lists)
                .collectList()
                .map(passbookResponseDTOList -> passbookResponseDTOList.stream()
                        .map(passbookResponseDTO -> gson.fromJson(
                                passbookResponseDTO.toString(), Passbook.class))
                        .toList())
                .flatMapIterable(passbookList -> passbookList)
                .collectList()
                .doOnNext(passbookList -> log.info(
                        "Passbook Loan Accounts Entry For Loan Adjustment: {}", passbookList));
    }

    private Mono<List<PassbookResponseDTO>> updateRepaymentScheduleForLoanAdjustmentPaidInstallment(
            List<PassbookResponseDTO> passbookResponseDTOList, String managementProcessId) {
        return Flux.fromIterable(passbookResponseDTOList)
                .filter(passbookResponseDTO -> passbookResponseDTO.getTransactionCode()
                        .equals(Constants.TRANSACTION_CODE_LOAN_ADJUSTMENT.getValue()) && passbookResponseDTO.getManagementProcessId().equals(managementProcessId))
                .collectList()
                .map(this::getFullyPaidInstallmentNos)
                .flatMap(tuple2 -> loanRepaymentScheduleUseCase
                        .updateInstallmentStatus(tuple2.getT2(), Status.STATUS_PAID.getValue(),
                                tuple2.getT1(), managementProcessId)
                        .collectList())
                .doOnNext(repaymentScheduleResponseDTO -> log.debug("repaymentScheduleResponseDTO {}",
                        repaymentScheduleResponseDTO))
                .map(repaymentScheduleResponseDTOS -> passbookResponseDTOList);
    }

    private Tuple2<String, List<Integer>> getFullyPaidInstallmentNos(
            List<PassbookResponseDTO> passbookResponseDTOList) {
        AtomicReference<String> loanAccountId = new AtomicReference<>();
        log.debug("passbookResponseDTOList : {}", passbookResponseDTOList);
        List<Integer> fulfilledInstallments = passbookResponseDTOList
                .stream()
                .peek(passbookResponseDTO -> log.debug("before filter passbook response dto : {}",
                        passbookResponseDTO))
                .filter(this::isThisInstallmentFullyPaid)
                .peek(passbookResponseDTO -> log.debug("after filter passbook response dto : {}",
                        passbookResponseDTO))
                .peek(passbookResponseDTO -> loanAccountId.set(passbookResponseDTO.getLoanAccountId()))
                .map(PassbookResponseDTO::getInstallNo)
                .peek(integer -> log.debug("fulfilled installments : {}", integer))
                .toList();
        log.debug("fulfilledInstallments : {}", fulfilledInstallments);
        Tuple2<String, List<Integer>> tuples;
        if (fulfilledInstallments.isEmpty()) {
            log.debug("I was here {}", loanAccountId);
            tuples = Tuples.of("", new ArrayList<>());
        } else
            tuples = Tuples.of(loanAccountId.get(), fulfilledInstallments);
        log.debug("Tuple2<String, List<Integer>> {}", tuples);
        return tuples;
    }

    private boolean isThisInstallmentFullyPaid(PassbookResponseDTO passbookResponseDTO) {
        if (passbookResponseDTO.getScRemainForThisInst() != null
                && passbookResponseDTO.getPrinRemainForThisInst() != null) {
            return passbookResponseDTO.getScRemainForThisInst().compareTo(BigDecimal.ZERO) == 0
                    && passbookResponseDTO.getPrinRemainForThisInst().compareTo(BigDecimal.ZERO) == 0;
        } else
            return false;
    }

    @Override
    public Mono<LoanAdjustmentMemberGridViewResponseDTO> detailViewOfLoanAdjustmentForAMember(
            LoanAdjustmentRequestDTO requestDTO) {
        return this.getNotApprovedAdjustedLoanAccountListForAMember(requestDTO.getMemberId())
                .zipWith(this.getApprovedAdjustedLoanAccountListForAMember(requestDTO.getMemberId()))
                .map(tuple -> {
                    List<AdjustedLoanAccount> adjustedLoanAccountList = new ArrayList<>();
                    adjustedLoanAccountList.addAll(Objects.requireNonNull(tuple.getT1()));
                    adjustedLoanAccountList.addAll(Objects.requireNonNull(tuple.getT2()));
                    return adjustedLoanAccountList;
                })
                .flatMap(adjustedLoanAccountList -> commonRepository
                        .getMemberEntityByMemberId(requestDTO.getMemberId())
                        .map(memberEntity -> LoanAdjustmentMemberGridViewResponseDTO.builder()
                                .memberId(requestDTO.getMemberId())
                                .memberNameEn(memberEntity.getMemberNameEn())
                                .memberNameBn(memberEntity.getMemberNameBn())
                                .adjustedLoanAccountList(adjustedLoanAccountList)
                                .totalCount(adjustedLoanAccountList.size())
                                .build()))
                .doOnNext(loanAdjustmentGridViewResponseDTO -> log.info(
                        "Loan Adjustment Grid View Response: {}",
                        loanAdjustmentGridViewResponseDTO))
                .doOnError(throwable -> log.error("Error in Loan Adjustment Grid View: {}",
                        throwable.getMessage()));
    }

    @Override
    public Mono<LoanAdjustmentDetailViewResponseDTO> detailsOfLoanAdjustmentCreationForAMember(LoanAdjustmentRequestDTO requestDTO) {
        return stagingDataUseCase.getStagingAccountDataListByMemberId(requestDTO.getMemberId())
                .collectList()
                .flatMap(stagingAccountDataList -> commonRepository.getMemberEntityByMemberId(requestDTO.getMemberId())
                        .zipWith(this.getSavingsAccountDetailsForLoanAdjustment(stagingAccountDataList))
                        .flatMap(memberEntityAndSavingsAccountList -> this.getLoanAccountDetailsForLoanAdjustment(stagingAccountDataList)
                                .map(loanAccountDetailsList -> LoanAdjustmentDetailViewResponseDTO.builder()
                                        .memberId(requestDTO.getMemberId())
                                        .memberNameEn(memberEntityAndSavingsAccountList.getT1().getMemberNameEn())
                                        .memberNameBn(memberEntityAndSavingsAccountList.getT1().getMemberNameBn())
                                        .loanAccountList(loanAccountDetailsList)
                                        .savingsAccountList(memberEntityAndSavingsAccountList.getT2())
                                        .build())))
                .flatMap(this::updateSavingsBalanceForLoanAdjustment);
    }

    private Mono<LoanAdjustmentDetailViewResponseDTO> updateSavingsBalanceForLoanAdjustment(LoanAdjustmentDetailViewResponseDTO responseDTO) {
        return Flux.fromIterable(responseDTO.getSavingsAccountList())
                .doOnNext(savingsAccountDetails -> {
                    savingsAccountDetails.setAvailableBalance(savingsAccountDetails.getAvailableBalance() == null ? BigDecimal.ZERO : savingsAccountDetails.getAvailableBalance());
                    savingsAccountDetails.setBalance(savingsAccountDetails.getBalance() == null ? BigDecimal.ZERO : savingsAccountDetails.getBalance());
                })
                .flatMap(savingsAccountDetails -> commonRepository.getCollectionStagingDataBySavingsAccountId(savingsAccountDetails.getSavingsAccountId())
                        .collectList()
                        .map(list -> {
                            savingsAccountDetails.setAvailableBalance(savingsAccountDetails.getAvailableBalance().add(!list.isEmpty() ? list.stream().map(CollectionStagingDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            savingsAccountDetails.setBalance(savingsAccountDetails.getBalance().add(!list.isEmpty() ? list.stream().map(CollectionStagingDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            return savingsAccountDetails;
                        }))
                .flatMap(savingsAccountDetails -> commonRepository.getStagingWithdrawDataBySavingsAccountId(savingsAccountDetails.getSavingsAccountId())
                        .collectList()
                        .map(list -> {
                            savingsAccountDetails.setAvailableBalance(savingsAccountDetails.getAvailableBalance().subtract(!list.isEmpty() ? list.stream().map(StagingWithdrawDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            savingsAccountDetails.setBalance(savingsAccountDetails.getBalance().subtract(!list.isEmpty() ? list.stream().map(StagingWithdrawDataEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO));
                            return savingsAccountDetails;
                        }))
                .collectList()
                .map(savingsAccountDetailsList -> {
                    responseDTO.setSavingsAccountList(savingsAccountDetailsList);
                    return responseDTO;
                });
    }

    private Mono<List<SavingsAccountDetails>> getSavingsAccountDetailsForLoanAdjustment(
            List<StagingAccountData> stagingAccountDataList) {
        return Flux.fromIterable(stagingAccountDataList)
                .filter(stagingAccountData -> !HelperUtil
                        .checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId()))
                .filter(stagingAccountData -> stagingAccountData.getSavingsProductType().equalsIgnoreCase("GS") || stagingAccountData.getSavingsProductType().equalsIgnoreCase("VS"))
                .flatMap(stagingAccountData -> savingsAccountUseCase.getSavingsAccountDetailsBySavingsAccountId(stagingAccountData.getSavingsAccountId()).zipWith(Mono.just(stagingAccountData)))
                .map(savingsAccountAndStagingAccountData -> SavingsAccountDetails.builder()
                        .savingsAccountId(savingsAccountAndStagingAccountData.getT2().getSavingsAccountId())
                        .savingsTypeId(savingsAccountAndStagingAccountData.getT1().getSavingsTypeId())
                        .savingsProductId(savingsAccountAndStagingAccountData.getT2().getSavingsProductCode())
                        .savingsProductNameEn(savingsAccountAndStagingAccountData.getT2().getSavingsProductNameEn())
                        .savingsProductNameBn(savingsAccountAndStagingAccountData.getT2().getSavingsProductNameBn())
                        .balance(savingsAccountAndStagingAccountData.getT2().getBalance())
                        .availableBalance(savingsAccountAndStagingAccountData.getT2().getSavingsAvailableBalance())
                        .build())
                .switchIfEmpty(Mono.just(SavingsAccountDetails.builder().build()))
                .collectList();
    }

    private Mono<List<LoanAccountDetails>> getLoanAccountDetailsForLoanAdjustment(
            List<StagingAccountData> stagingAccountDataList) {
        return Flux.fromIterable(stagingAccountDataList)
                .filter(stagingAccountData -> !HelperUtil
                        .checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
                .flatMap(stagingAccountData -> collectionStagingDataPersistencePort.getCollectionStagingDataByLoanAccountId(stagingAccountData.getLoanAccountId())
                        .switchIfEmpty(Mono.just(CollectionStagingData.builder().amount(BigDecimal.ZERO).build()))
                        .map(collectionStagingData -> LoanAccountDetails.builder()
                                .loanAccountId(stagingAccountData.getLoanAccountId())
                                .loanProductId(stagingAccountData.getProductCode())
                                .loanProductNameEn(stagingAccountData.getProductNameEn())
                                .loanProductNameBn(stagingAccountData.getProductNameBn())
                                .loanAmount(stagingAccountData.getLoanAmount())
                                .serviceCharge(stagingAccountData.getServiceCharge())
                                .totalLoanAmount(stagingAccountData.getLoanAmount()
                                        .add(stagingAccountData.getServiceCharge()))
                                .principalPaid(stagingAccountData.getTotalPrincipalPaid())
                                .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
                                .totalPaid(stagingAccountData.getTotalPrincipalPaid()
                                        .add(stagingAccountData.getTotalServiceChargePaid()))
                                .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
                                .serviceChargeRemaining(
                                        stagingAccountData.getTotalServiceChargeRemaining())
                                .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(
                                        stagingAccountData.getTotalServiceChargeRemaining()))
                                .accountOutstanding(stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()).subtract(collectionStagingData.getAmount()))
                                .build())
                ).collectList();

//        return stagingAccountDataList.stream()
//                .filter(stagingAccountData -> !HelperUtil
//                        .checkIfNullOrEmpty(stagingAccountData.getLoanAccountId()))
//                .map(stagingAccountData -> LoanAccountDetails.builder()
//                        .loanAccountId(stagingAccountData.getLoanAccountId())
//                        .loanProductId(stagingAccountData.getProductCode())
//                        .loanProductNameEn(stagingAccountData.getProductNameEn())
//                        .loanProductNameBn(stagingAccountData.getProductNameBn())
//                        .loanAmount(stagingAccountData.getLoanAmount())
//                        .serviceCharge(stagingAccountData.getServiceCharge())
//                        .totalLoanAmount(stagingAccountData.getLoanAmount()
//                                .add(stagingAccountData.getServiceCharge()))
//                        .principalPaid(stagingAccountData.getTotalPrincipalPaid())
//                        .serviceChargePaid(stagingAccountData.getTotalServiceChargePaid())
//                        .totalPaid(stagingAccountData.getTotalPrincipalPaid()
//                                .add(stagingAccountData.getTotalServiceChargePaid()))
//                        .principalRemaining(stagingAccountData.getTotalPrincipalRemaining())
//                        .serviceChargeRemaining(
//                                stagingAccountData.getTotalServiceChargeRemaining())
//                        .totalDue(stagingAccountData.getTotalPrincipalRemaining().add(
//                                stagingAccountData.getTotalServiceChargeRemaining()))
//                        .accountOutstanding(stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining()).subtract(collectionAmount))
//                        .build())
//                .toList();
    }

    private Mono<List<AdjustedLoanAccount>> getApprovedAdjustedLoanAccountListForAMember(String memberId) {
        return transactionUseCase.getLoanAdjustedTransactionsForLoanAccountsOfMember(memberId)
                .doOnNext(transactionList -> log.debug(
                        "Loan Account Transaction List for Loan Adjustment: {}",
                        transactionList))
                .flatMap(this::getLoanAdjustedDataForLoanAccounts);
    }

    private Mono<List<AdjustedLoanAccount>> getLoanAdjustedDataForLoanAccounts(
            List<Transaction> loanAdjustedTransactionList) {
        List<String> loanAdjustmentProcessIdList = loanAdjustedTransactionList.stream()
                .map(Transaction::getLoanAdjustmentProcessId).toList();
        return transactionUseCase.getSavingsAccountsForLoanAdjustedTransactions(loanAdjustmentProcessIdList)
                .doOnNext(transactionList -> log.debug(
                        "Savings Account Transaction List for Loan Adjustment: {}",
                        transactionList))
                .map(savingsTransactionList -> loanAdjustedTransactionList.stream()
                        .map(loanTransaction -> AdjustedLoanAccount.builder()
                                .loanAccountId(loanTransaction.getLoanAccountId())
                                .adjustmentDate(loanTransaction.getTransactionDate())
                                .adjustedAmount(loanTransaction.getAmount())
                                .status(loanTransaction.getStatus())
                                .adjustedSavingsAccountList(this
                                        .buildAdjustedSavingsAccountListFromTransaction(
                                                loanTransaction,
                                                savingsTransactionList))
                                .build())
                        .sorted(Comparator.comparing(AdjustedLoanAccount::getLoanAccountId))
                        .toList());
    }

    private List<AdjustedSavingsAccount> buildAdjustedSavingsAccountListFromTransaction(Transaction loanTransaction,
                                                                                        List<Transaction> savingsTransactionList) {
        return savingsTransactionList.stream()
                .filter(savingsTransaction -> loanTransaction.getLoanAdjustmentProcessId()
                        .equals(savingsTransaction.getLoanAdjustmentProcessId()))
                .map(savingsTransaction -> AdjustedSavingsAccount.builder()
                        .savingsAccountId(savingsTransaction.getSavingsAccountId())
                        .amount(savingsTransaction.getAmount())
                        .build())
                .sorted(Comparator.comparing(AdjustedSavingsAccount::getSavingsAccountId))
                .toList();
    }

    private Mono<List<AdjustedLoanAccount>> getNotApprovedAdjustedLoanAccountListForAMember(String memberId) {
        return port.getLoanAdjustmentDataByMemberId(memberId)
                .collectList()
                .doOnNext(loanAdjustmentDataList -> log.debug("Loan Adjustment Data For Member: {}",
                        loanAdjustmentDataList))
                .map(this::buildAdjustmentLoanAccountForAMember);
    }

    private List<AdjustedLoanAccount> buildAdjustmentLoanAccountForAMember(
            List<LoanAdjustmentData> loanAdjustmentDataList) {
        return loanAdjustmentDataList.stream()
                .filter(loanAdjustmentData -> !HelperUtil
                        .checkIfNullOrEmpty(loanAdjustmentData.getLoanAccountId()))
                .map(loanAccountAdjustedData -> AdjustedLoanAccount.builder()
                        .oid(loanAccountAdjustedData.getOid())
                        .loanAccountId(loanAccountAdjustedData.getLoanAccountId())
                        .adjustmentDate(loanAccountAdjustedData.getCreatedOn().toLocalDate())
                        .adjustedAmount(loanAccountAdjustedData.getAmount())
                        .status(loanAccountAdjustedData.getStatus())
                        .adjustedSavingsAccountList(this
                                .buildAdjustedSavingsAccountListFromLoanAdjustmentData(
                                        loanAccountAdjustedData,
                                        loanAdjustmentDataList))
                        .build())
                .sorted(Comparator.comparing(AdjustedLoanAccount::getLoanAccountId))
                .toList();
    }

    private List<AdjustedSavingsAccount> buildAdjustedSavingsAccountListFromLoanAdjustmentData(
            LoanAdjustmentData loanAccountAdjustedData, List<LoanAdjustmentData> loanAdjustmentDataList) {
        return loanAdjustmentDataList.stream()
                .filter(loanAdjustmentData -> !HelperUtil
                        .checkIfNullOrEmpty(loanAdjustmentData.getSavingsAccountId())
                        && loanAccountAdjustedData.getLoanAdjustmentProcessId().equals(
                        loanAdjustmentData.getLoanAdjustmentProcessId()))
                .map(savingsAccountAdjustedData -> AdjustedSavingsAccount.builder()
                        .savingsAccountId(savingsAccountAdjustedData.getSavingsAccountId())
                        .amount(savingsAccountAdjustedData.getAmount())
                        .build())
                .sorted(Comparator.comparing(AdjustedSavingsAccount::getSavingsAccountId))
                .toList();
    }

    @Override
    public Mono<LoanAdjustmentResponseDTO> submitLoanAdjustmentDataForAuthorization(LoanAdjustmentRequestDTO requestDTO) {
        return commonRepository.getOfficeIdBySamityId(requestDTO.getSamityId())
                .flatMap(officeId -> this.getManagementProcessIdAndValidateSamityLoanAdjustmentDataForSubmission(officeId, requestDTO.getSamityId()))
                .flatMap(managementProcessId -> port.updateStatusToSubmitLoanAdjustmentDataForAuthorization(managementProcessId, requestDTO.getSamityId(), requestDTO.getLoginId()))
                .map(data -> LoanAdjustmentResponseDTO.builder()
                        .userMessage("Loan Adjustment Data is Submitted For Authorization Successfully for Samity")
                        .build())
                .doOnNext(response -> log.info("Loan Adjustment Response: {}", response))
                .doOnError(throwable -> log.error("Error Creating Loan Adjustment: {}", throwable.getMessage()));
    }

    @Override
    public Mono<LoanAdjustmentResponseDTO> submitLoanAdjustmentDataForAuthorization(String managementProcessId,
                                                                                    String processId,
                                                                                    String loginId) {
        return port.updateStatusToSubmitLoanAdjustmentDataForAuthorizationByManagementProcessId(managementProcessId, processId, loginId)
                .map(data -> LoanAdjustmentResponseDTO.builder()
                        .userMessage("Loan Adjustment Data is Submitted For Authorization Successfully")
                        .build())
                .doOnNext(response -> log.info("Loan Adjustment submit Response: {}", response))
                .doOnError(throwable -> log.error("Error submitting Loan Adjustment: {}", throwable.getMessage()));
    }

    private Mono<String> getManagementProcessIdAndValidateSamityLoanAdjustmentDataForSubmission(String officeId, String samityId) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), officeId)
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList()
                        .filter(officeEventList -> officeEventList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                        .filter(officeEventList -> !officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Completed For Office"))))
                .flatMap(officeEventList -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcess.get().getManagementProcessId(), samityId)
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .map(SamityEventTracker::getSamityEvent)
                        .collectList()
                        .filter(samityEventList -> samityEventList.contains(SamityEvents.LOAN_ADJUSTED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Loan Adjustment Data Found for Samity"))))
                .map(samityEventList -> managementProcess.get().getManagementProcessId());
    }

    @Override
    public Mono<List<LoanAdjustmentData>> getLoanAdjustmentDataBySamity(String samityId) {
        return port.getLoanAdjustmentDataBySamity(samityId)
                .collectList();
    }

    @Override
    public Mono<Map<String, BigDecimal>> getTotalLoanAdjustmentAmountForSamityIdList(List<String> samityIdList) {
        return port.getAllLoanAdjustmentDataBySamityIdList(samityIdList)
                .map(loanAdjustmentDataList -> {
                    Map<String, BigDecimal> samityWithTotalLoanAdjustment = new HashMap<>();
                    samityIdList.forEach(samityId -> {
                        BigDecimal totalAmount = loanAdjustmentDataList.stream()
                                .filter(loanAdjustmentData -> loanAdjustmentData
                                        .getSamityId().equals(samityId)
                                        && !HelperUtil.checkIfNullOrEmpty(
                                        loanAdjustmentData
                                                .getLoanAccountId()))
                                .map(LoanAdjustmentData::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        samityWithTotalLoanAdjustment.put(samityId, totalAmount);
                    });
                    return samityWithTotalLoanAdjustment;
                });
    }

    @Override
    public Mono<Map<String, BigDecimal>> getTotalSavingsAdjustmentAmountForSamityIdList(List<String> samityIdList) {
        return port.getAllLoanAdjustmentDataBySamityIdList(samityIdList)
                .map(loanAdjustmentDataList -> {
                    Map<String, BigDecimal> samityWithTotalSavingsAdjustment = new HashMap<>();
                    samityIdList.forEach(samityId -> {
                        BigDecimal totalAmount = loanAdjustmentDataList.stream()
                                .filter(loanAdjustmentData -> loanAdjustmentData
                                        .getSamityId().equals(samityId)
                                        && !HelperUtil.checkIfNullOrEmpty(
                                        loanAdjustmentData
                                                .getSavingsAccountId()))
                                .map(LoanAdjustmentData::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        samityWithTotalSavingsAdjustment.put(samityId, totalAmount);
                    });
                    return samityWithTotalSavingsAdjustment;
                });
    }

    @Override
    public Mono<String> lockSamityForAuthorization(String samityId, String loginId) {
        return port.lockSamityForAuthorization(samityId, loginId);
    }

    @Override
    public Mono<String> unlockSamityForAuthorization(String samityId, String loginId) {
        return port.unlockSamityForAuthorization(samityId, loginId);
    }

    @Override
    public Mono<List<String>> getSamityIdListLockedByUserForAuthorization(String lockedBy) {
        return port.getSamityIdListLockedByUserForAuthorization(lockedBy);
    }

    @Override
    public Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataBySamityIdList(List<String> samityIdList) {
        return port.getAllLoanAdjustmentDataBySamityIdList(samityIdList);
    }

    @Override
    public Mono<String> validateAndUpdateLoanAdjustmentDataForRejectionBySamityId(String managementProcessId,
                                                                                  String samityId, String loginId) {
        return port.validateAndUpdateLoanAdjustmentDataForRejectionBySamityId(managementProcessId, samityId,
                loginId);
    }

    @Override
    public Mono<String> validateAndUpdateLoanAdjustmentDataForUnauthorizationBySamityId(String managementProcessId,
                                                                                        String samityId, String loginId) {
        return port.validateAndUpdateLoanAdjustmentDataForUnauthorizationBySamityId(managementProcessId,
                        samityId, loginId)
                .map(response -> samityId);
    }

    @Override
    public Mono<LoanAdjustmentMemberGridViewResponseDTO> getAdjustedLoanAccountListByManagementProcessId(LoanAdjustmentRequestDTO requestDTO) {
        return port.getAllLoanAdjustmentDataByManagementProcessId(requestDTO.getManagementProcessId())
                .doOnNext(loanAdjustmentDataList -> log.info("Loan Adjustment Data List: {}", loanAdjustmentDataList))
                .flatMapMany(loanAdjustmentDataList -> Flux.fromIterable(loanAdjustmentDataList))
                .filterWhen(loanAdjustment -> Mono.just(loanAdjustment.getProcessId().equals(requestDTO.getProcessId())))
                .filterWhen(loanAdjustment -> Mono.just(loanAdjustment.getCurrentVersion().equals(requestDTO.getCurrentVersion())))
                .switchIfEmpty(stagingCollectionDataArchiveAdapter.getLoanAdjustmentDataByManagementProcessId(requestDTO.getManagementProcessId())
                        .flatMapMany(Flux::fromIterable)
                        .filterWhen(loanAdjustment -> Mono.just(loanAdjustment.getProcessId().equals(requestDTO.getProcessId())))
                        .filterWhen(loanAdjustment -> Mono.just(loanAdjustment.getCurrentVersion().equals(requestDTO.getCurrentVersion())))

                )
                .collectList()
                .map(this::buildAdjustmentLoanAccountForAMember)
                .flatMap(adjustedLoanAccountList -> commonRepository.getMemberEntityByMemberId(requestDTO.getMemberId())
                        .doOnNext(memberEntity -> log.info("Member Entity: {}", memberEntity))
                        .map(memberEntity -> LoanAdjustmentMemberGridViewResponseDTO.builder()
                                .memberId(requestDTO.getMemberId())
                                .memberNameEn(memberEntity.getMemberNameEn())
                                .memberNameBn(memberEntity.getMemberNameBn())
                                .adjustedLoanAccountList(adjustedLoanAccountList)
                                .totalCount(adjustedLoanAccountList.size())
                                .build()))
                .doOnNext(loanAdjustmentGridViewResponseDTO -> log.info("Loan Adjustment List Response: {}", loanAdjustmentGridViewResponseDTO))
                .doOnError(throwable -> log.error("Error in Loan Adjustment List: {}", throwable.getMessage()));
    }

    @Override
    public Mono<CollectionGridResponse> AdjustmentCollectionGridView(LoanAdjustmentRequestDTO request) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(request.getOfficeId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Management Process Found for Office")))
                .flatMap(managementProcessTracker -> getAccountDataInfo(request, managementProcessTracker.getManagementProcessId())
                        .collectList()
                        .map(dataList -> CollectionGridResponse.builder()
                                .mfiId(managementProcessTracker.getMfiId())
                                .officeId(managementProcessTracker.getOfficeId())
                                .officeNameBn(managementProcessTracker.getOfficeNameBn())
                                .officeNameEn(managementProcessTracker.getOfficeNameEn())
                                .businessDate(managementProcessTracker.getBusinessDate())
                                .businessDay(managementProcessTracker.getBusinessDay())
                                .data(dataList)
                                .build())
                        .flatMap(collectionGridResponse -> getLoanAdjustmentCount(request, managementProcessTracker.getManagementProcessId())
                                .map(count -> {
                                    log.info("Total Count: {}", count);
                                    collectionGridResponse.setTotalCount(count);
                                    return collectionGridResponse;
                                }))
                );
    }

    private Mono<String> getLoginIdByFieldOfficerId(LoanAdjustmentRequestDTO request) {
        return employeePersistencePort.getEmployeeByEmployeeId(request.getFieldOfficerId().trim())
                .map(employee -> employee.getLoginId().trim());
    }

    private Mono<String> setRequestLoginId(LoanAdjustmentRequestDTO request) {
        return HelperUtil.checkIfNullOrEmpty(request.getFieldOfficerId()) ? Mono.just(request.getFieldOfficerId().trim()) : getLoginIdByFieldOfficerId(request);
    }

    private Flux<AccountDataInfo> getAccountDataInfo(LoanAdjustmentRequestDTO request, String managementProcessId) {
        return setRequestLoginId(request)
                .flatMapMany(loginId -> port.getAllAdjustmentCollectionDataByLoginId(managementProcessId, loginId, request.getLimit(), request.getOffset())
                        .switchIfEmpty(Mono.just(LoanAdjustmentData.builder().build()))
                        .filter(loanAdjustmentData -> loanAdjustmentData.getSamityId() != null && loanAdjustmentData.getSamityId().startsWith(request.getOfficeId()))
                        .switchIfEmpty(Mono.just(LoanAdjustmentData.builder().build()))
                        .flatMap(loanAdjustmentData -> stagingDataUseCase.getStagingDataByMemberId(loanAdjustmentData.getMemberId())
                                .map(stagingData -> buildAccountDataInfo(loanAdjustmentData, stagingData))));
    }

    private Mono<Long> getLoanAdjustmentCount(LoanAdjustmentRequestDTO request, String managementProcessId) {
        return setRequestLoginId(request).flatMap(loginId -> port.getCountLoanAdjustment(managementProcessId, loginId));
    }

    private AccountDataInfo buildAccountDataInfo(LoanAdjustmentData loanAdjustmentData, StagingData stagingData) {
        return AccountDataInfo.builder()
                .btnViewEnabled(Status.STATUS_YES.getValue())
//                .btnOpenEnabled("")
                .btnEditEnabled(loanAdjustmentData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()) || loanAdjustmentData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) ? Status.STATUS_YES.getValue() : Status.STATUS_NO.getValue())
                .btnSubmitEnabled(loanAdjustmentData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()) || loanAdjustmentData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) ? Status.STATUS_YES.getValue() : Status.STATUS_NO.getValue())
//                .btnCommitEnabled("")
                .oid(loanAdjustmentData.getOid())
                .stagingDataId(stagingData.getStagingDataId())
                .memberId(stagingData.getMemberId())
                .memberNameEn(stagingData.getMemberNameEn())
                .memberNameBn(stagingData.getMemberNameBn())
                .samityId(stagingData.getSamityId())
                .samityNameEn(stagingData.getSamityNameEn())
                .samityNameBn(stagingData.getSamityNameBn())
                .loanAccountId(loanAdjustmentData.getLoanAccountId())
                .savingsAccountId(loanAdjustmentData.getSavingsAccountId())
                .accountType(loanAdjustmentData.getAccountType())
                .amount(loanAdjustmentData.getAmount())
                .status(loanAdjustmentData.getStatus())
                .createdBy(loanAdjustmentData.getCreatedBy())
                .build();
    }

    @Override
    public Mono<AdjustmentCollectionDetailViewResponse> AdjustmentCollectionDetailView(LoanAdjustmentRequestDTO request) {
        return port.getLoanAdjustmentCollectionDataByOid(request.getId())
                .flatMap(loanAdjustmentData -> Mono.zip(
                                commonRepository.getMemberEntityByMemberId(loanAdjustmentData.getMemberId())
                                        .map(this::extractMobileNumberFromMobileDetails),
                                commonRepository.getSamityBySamityId(loanAdjustmentData.getSamityId()),
                                this.buildAdjustmentLoanAccountForDetailsView(loanAdjustmentData)
                                        .doOnRequest(l -> log.info("Requesting {} Loan Account Data For Details View", l))
                                        .doOnNext(loanAccountList -> log.info("Loan Account Data For Details View: {}", loanAccountList)))
                        .doOnNext(tuple -> log.info("Loan Adjustment Data For tuple : {}", tuple))
                        .map(tuple -> AdjustmentCollectionDetailViewResponse.builder()
                                .userMessage("Loan Adjustment Data Fetch Successfully")
                                .data(AdjustmentDetailViewResponse.builder()
                                        .memberInfo(MemberInfoDTO.builder()
                                                .memberId(loanAdjustmentData.getMemberId())
                                                .memberNameEn(tuple.getT1().getMemberNameEn())
                                                .memberNameBn(tuple.getT1().getMemberNameBn())
                                                .mobile(tuple.getT1().getMobile())
                                                .companyMemberId(tuple.getT1().getCompanyMemberId())
                                                .registerBookSerialId(tuple.getT1().getRegisterBookSerialId())
                                                .gender(tuple.getT1().getGender())
                                                .fatherNameEn(tuple.getT1().getFatherNameEn())
                                                .fatherNameBn(tuple.getT1().getFatherNameBn())
                                                .maritalStatus(tuple.getT1().getMaritalStatus())
                                                .spouseNameBn(tuple.getT1().getSpouseNameBn())
                                                .spouseNameEn(tuple.getT1().getSpouseNameEn())
                                                .build())
                                        .samityInfo(Samity.builder()
                                                .samityId(loanAdjustmentData.getSamityId())
                                                .samityNameEn(tuple.getT2().getSamityNameEn())
                                                .samityNameBn(tuple.getT2().getSamityNameBn())
                                                .companySamityId(tuple.getT2().getCompanySamityId())
                                                .samityDay(tuple.getT2().getSamityDay())
                                                .fieldOfficerId(tuple.getT2().getFieldOfficerId())
                                                .fieldOfficerNameEn(tuple.getT2().getFieldOfficerNameEn())
                                                .fieldOfficerNameBn(tuple.getT2().getFieldOfficerNameBn())
                                                .build())
                                        .adjustedLoanAccountInfo(tuple.getT3())
                                        .build())
                                .build()));
    }

    private Mono<List<AdjustedLoanAccount>> buildAdjustmentLoanAccountForDetailsView(LoanAdjustmentData loanAdjustmentData) {
        return stagingDataUseCase.getStagingAccountDataByLoanAccountId(loanAdjustmentData.getLoanAccountId())
                .zipWith(port.getAllAdjustmentByManagementProcessIdAndLoanAdjustmentProcessId(loanAdjustmentData.getManagementProcessId(), loanAdjustmentData.getLoanAdjustmentProcessId())
                        .collectList())
                .flatMapMany(tuple2 -> Flux.fromIterable(tuple2.getT2())
                        .filter(data -> !HelperUtil.checkIfNullOrEmpty(data.getLoanAccountId()))
                        .flatMap(loanAccountAdjustedData -> buildAdjustedLoanAccount(loanAccountAdjustedData, tuple2.getT1(), tuple2.getT2()))
                )
                .sort(Comparator.comparing(AdjustedLoanAccount::getLoanAccountId))
                .collectList();
    }

    private Mono<AdjustedLoanAccount> buildAdjustedLoanAccount(LoanAdjustmentData loanAccountAdjustedData, StagingAccountData stagingAccountData, List<LoanAdjustmentData> loanAdjustmentDataList) {
        return getAdjustedSavingsAccountData(loanAccountAdjustedData, loanAdjustmentDataList)
                .zipWith(collectionStagingDataPersistencePort.getCollectionStagingDataByLoanAccountId(loanAccountAdjustedData.getLoanAccountId())
                        .switchIfEmpty(Mono.just(CollectionStagingData.builder().build())))
                .flatMap(tuple2 -> {
                    BigDecimal principalRemaining = stagingAccountData.getTotalPrincipalRemaining() == null ? BigDecimal.ZERO : stagingAccountData.getTotalPrincipalRemaining();
                    BigDecimal serviceChargeRemaining = stagingAccountData.getTotalServiceChargeRemaining() == null ? BigDecimal.ZERO : stagingAccountData.getTotalServiceChargeRemaining();
                    BigDecimal collectionStagingAmount = tuple2.getT2().getAmount() == null ? BigDecimal.ZERO : tuple2.getT2().getAmount();
                    BigDecimal loanAccountOutstanding = principalRemaining.add(serviceChargeRemaining).subtract(collectionStagingAmount);
                    return loanWaiverPersistencePort.getLoanWaiverByLoanAccountId(loanAccountAdjustedData.getLoanAccountId())
                            .switchIfEmpty(Mono.just(LoanWaiver.builder().build()))
                            .zipWith(loanRebatePersistencePort.getLoanRebateByLoanAccountId(loanAccountAdjustedData.getLoanAccountId())
                                    .switchIfEmpty(Mono.just(LoanRebate.builder().build())))
                            .map(waiverAndRebate -> AdjustedLoanAccount.builder()
                                    .oid(loanAccountAdjustedData.getOid())
                                    .loanAccountId(loanAccountAdjustedData.getLoanAccountId())
                                    .totalPrincipalRemaining(principalRemaining)
                                    .totalServiceChargeRemaining(serviceChargeRemaining)
                                    .accountOutstanding(getOutstanding(waiverAndRebate, loanAccountOutstanding))
                                    .totalDue(stagingAccountData.getTotalDue() == null ? BigDecimal.ZERO : stagingAccountData.getTotalDue())
                                    .adjustmentDate(loanAccountAdjustedData.getCreatedOn().toLocalDate())
                                    .adjustedAmount(loanAccountAdjustedData.getAmount())
                                    .status(loanAccountAdjustedData.getStatus())
                                    .adjustedSavingsAccountList(tuple2.getT1())
                                    .build());
                        });
    }

    private static BigDecimal getOutstanding(Tuple2<LoanWaiver, LoanRebate> waiverAndRebate, BigDecimal loanAccountOutstanding) {
        BigDecimal rebateAmount = waiverAndRebate.getT2().getRebateAmount() == null ? BigDecimal.ZERO : waiverAndRebate.getT2().getRebateAmount();
        BigDecimal waiverAmount = waiverAndRebate.getT1().getWaivedAmount() == null ? BigDecimal.ZERO : waiverAndRebate.getT1().getWaivedAmount();
        return (waiverAmount.compareTo(BigDecimal.ZERO) == 0 && rebateAmount.compareTo(BigDecimal.ZERO) == 0)
                ? loanAccountOutstanding
                : BigDecimal.ZERO;
    }

    private Mono<List<AdjustedSavingsAccount>> getAdjustedSavingsAccountData(LoanAdjustmentData loanAdjustmentData, List<LoanAdjustmentData> loanAdjustmentDataList) {
        return Flux.fromIterable(loanAdjustmentDataList)
                .filter(data -> !HelperUtil.checkIfNullOrEmpty(data.getSavingsAccountId()) && data.getLoanAdjustmentProcessId().equals(loanAdjustmentData.getLoanAdjustmentProcessId()))
                .flatMap(savingsAccount -> Mono.zip(
                                Mono.just(savingsAccount),
                                stagingDataUseCase.getStagingAccountDataBySavingsAccountId(savingsAccount.getSavingsAccountId()),
                                collectionStagingDataPersistencePort.getCollectionStagingDataBySavingsAccountId(savingsAccount.getSavingsAccountId())
                                        .switchIfEmpty(Mono.just(CollectionStagingData.builder().build())),
                                withdrawStagingDataPersistencePort.getWithdrawStagingDataBySavingsAccountId(savingsAccount.getSavingsAccountId())
                                        .switchIfEmpty(Mono.just(StagingWithdrawData.builder().build())))
                        .map(tuple -> AdjustedSavingsAccount.builder()
                                .savingsAccountId(savingsAccount.getSavingsAccountId())
                                .amount(savingsAccount.getAmount())
                                .savingsAvailableBalance(setSavingAvailableBalanceData(tuple.getT2(), tuple.getT3(), tuple.getT4()))
                                .build()))
                .sort(Comparator.comparing(AdjustedSavingsAccount::getSavingsAccountId))
                .collectList();
    }

    private BigDecimal setSavingAvailableBalanceData(StagingAccountData stagingAccountData, CollectionStagingData collectionStagingData, StagingWithdrawData stagingWithdrawData) {
        BigDecimal savingsAvailableBalance = stagingAccountData.getSavingsAvailableBalance() == null ? BigDecimal.ZERO : stagingAccountData.getSavingsAvailableBalance();
        BigDecimal collectionAmount = collectionStagingData.getAmount() == null ? BigDecimal.ZERO : collectionStagingData.getAmount();
        BigDecimal withdrawAmount = stagingWithdrawData.getAmount() == null ? BigDecimal.ZERO : stagingWithdrawData.getAmount();
        log.info("Savings Available Balance: {}", savingsAvailableBalance);
        log.info("Collection Amount: {}", collectionAmount);
        log.info("Withdraw Amount: {}", withdrawAmount);
        return savingsAvailableBalance.add(collectionAmount).subtract(withdrawAmount);
    }

    private Mono<List<Transaction>> createTransactionForLoanAdjustment(
                        List<LoanAdjustmentData> loanAdjustmentDataList, String managementProcessId, String mfiId,
                        String officeId, String loginId, String transactionProcessId, String samityId) {
                return Flux.fromIterable(loanAdjustmentDataList)
                                .map(loanAdjustmentData -> Transaction.builder()
                                                .transactionId(UUID.randomUUID().toString())
                                                .mfiId(mfiId)
                                                .managementProcessId(managementProcessId)
                                                .processId(transactionProcessId)
                                                .loanAdjustmentProcessId(loanAdjustmentData.getLoanAdjustmentProcessId())
                                                .officeId(officeId)
                                                .memberId(loanAdjustmentData.getMemberId())
                                                .accountType(loanAdjustmentData.getAccountType())
                                                .loanAccountId(loanAdjustmentData.getLoanAccountId())
                                                .savingsAccountId(loanAdjustmentData.getSavingsAccountId())
                                                .amount(loanAdjustmentData.getAmount())
                                                .transactionCode(Constants.TRANSACTION_CODE_LOAN_ADJUSTMENT.getValue())
                                                .paymentMode("ADJUSTMENT")
                                                .status(Status.STATUS_APPROVED.getValue())
                                                .transactionDate(LocalDate.now())
                                                .transactedBy(loginId)
                                                .createdBy(loginId)
                                                .createdOn(LocalDateTime.now())
                                                .samityId(samityId)
                                                .build())
                                .collectList()
                                .doOnNext(transactionList -> log.debug("Transaction List For Loan Adjustment: {}",
                                                transactionList))
                                .flatMap(transactionUseCase::createTransactionEntryForLoanAdjustmentForSamity);
        }

    private Mono<List<LoanAdjustmentData>> validateAndUpdateLoanAdjustmentDataForAuthorization(
            LoanAdjustmentRequestDTO requestDTO) {
        return port.getLoanAdjustmentDataBySamity(requestDTO.getSamityId())
                .filter(loanAdjustmentData -> loanAdjustmentData.getAdjustmentType().equals(CollectionType.ADJUSTMENT.getValue()))
                .collectList()
                .filter(loanAdjustmentDataList -> !loanAdjustmentDataList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Adjustment Data Not Found for Samity")))
                .filter(loanAdjustmentDataList -> loanAdjustmentDataList.stream()
                        .noneMatch(loanAdjustmentData -> !HelperUtil
                                .checkIfNullOrEmpty(loanAdjustmentData.getStatus())
                                && loanAdjustmentData.getStatus().equals(
                                Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Adjustment Data is Already Authorized for Samity")))
                .filter(loanAdjustmentDataList -> !loanAdjustmentDataList.isEmpty()
                        && loanAdjustmentDataList.stream().allMatch(
                        loanAdjustmentData -> !HelperUtil.checkIfNullOrEmpty(
                                loanAdjustmentData.getStatus())
                                && (loanAdjustmentData.getStatus()
                                .equals(Status.STATUS_SUBMITTED
                                        .getValue())
                                || loanAdjustmentData
                                .getStatus()
                                .equals(Status.STATUS_UNAUTHORIZED
                                        .getValue()))))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Adjustment Data is not Submitted for Authorization")))
                .filter(loanAdjustmentDataList -> loanAdjustmentDataList.stream()
                        .allMatch(loanAdjustmentData -> !HelperUtil
                                .checkIfNullOrEmpty(loanAdjustmentData.getIsLocked())
                                && loanAdjustmentData.getIsLocked().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST,
                        "Loan Adjustment Data is Not Locked for Samity")))
                .flatMap(loanAdjustmentDataList -> port
                        .updateStatusOfLoanAdjustmentDataForAuthorization(
                                requestDTO.getSamityId(), requestDTO.getLoginId()));
    }

    private List<LoanAdjustmentData> buildLoanAdjustmentDataFromRequest(LoanAdjustmentRequestDTO requestDTO,
                                                                        String managementProcessId, String samityEventId, String isNew) {
        return requestDTO.getData().stream()
                .map(adjustedLoanData -> {
                    final String loanAdjustmentProcessId = UUID.randomUUID().toString();
                    BigDecimal totalAdjustedAmount = adjustedLoanData.getAdjustedAccountList()
                            .stream()
                            .map(AdjustedAccount::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    List<LoanAdjustmentData> loanAdjustmentDataList = new ArrayList<>();
                    // Create Loan Account Entry For Loan Adjustment
                    loanAdjustmentDataList.add(
                            buildLoanAdjustmentData(
                                    requestDTO,
                                    managementProcessId,
                                    samityEventId,
                                    loanAdjustmentProcessId,
                                    adjustedLoanData.getLoanAccountId(),
                                    null,
                                    "Loan",
                                    totalAdjustedAmount,
                                    isNew,
                                    ObjectUtils.isNotEmpty(requestDTO.getCurrentVersion()) ? requestDTO.getCurrentVersion() : 1
                            ));
                    // Create Savings Account Entry for Loan Adjustment
                    loanAdjustmentDataList.addAll(adjustedLoanData.getAdjustedAccountList().stream()
                            .map(adjustedAccount ->
                                    buildLoanAdjustmentData(
                                            requestDTO,
                                            managementProcessId,
                                            samityEventId,
                                            loanAdjustmentProcessId,
                                            null,
                                            adjustedAccount.getSavingsAccountId(),
                                            "Savings",
                                            adjustedAccount.getAmount(),
                                            isNew,
                                            ObjectUtils.isNotEmpty(requestDTO.getCurrentVersion()) ? requestDTO.getCurrentVersion() : 1
                                    ))
                            .toList());
                    return loanAdjustmentDataList;
                })
                .flatMap(List::stream)
                .toList();
    }

    private LoanAdjustmentData buildLoanAdjustmentData(LoanAdjustmentRequestDTO requestDTO,
                                                       String managementProcessId,
                                                       String samityEventId,
                                                       String loanAdjustmentProcessId,
                                                       String loanAccountId,
                                                       String savingsAccountId,
                                                       String accountType,
                                                       BigDecimal amount,
                                                       String isNew,
                                                       Integer currentVersion
    ) {

        return LoanAdjustmentData.builder()
                .managementProcessId(managementProcessId)
                .processId(requestDTO.getProcessId() != null ? requestDTO.getProcessId() : loanAdjustmentProcessId)
                .loanAdjustmentDataId(UUID.randomUUID().toString())
                .loanAdjustmentProcessId(requestDTO.getProcessId() != null ? requestDTO.getProcessId() : loanAdjustmentProcessId)
                .samityId(requestDTO.getSamityId())
                .memberId(requestDTO.getMemberId())
                .loanAccountId(loanAccountId)
                .savingsAccountId(savingsAccountId)
                .accountType(accountType)
                .amount(amount)
                .status(Status.STATUS_STAGED.getValue())
                .adjustmentType(requestDTO.getAdjustmentType() == null ? CollectionType.ADJUSTMENT.getValue() : requestDTO.getAdjustmentType())
                .createdBy(requestDTO.getLoginId())
                .createdOn(LocalDateTime.now())
                .isNew(isNew)
                .currentVersion(currentVersion)
                .isSubmitted("No")
                .isLocked("No")
                .updatedBy(isNew.equalsIgnoreCase("Yes") ? null : requestDTO.getLoginId())
                .updatedOn(isNew.equalsIgnoreCase("Yes") ? null : LocalDateTime.now())
                .build();

    }

    private Boolean verifyLoanAdjustmentRequest(LoanAdjustmentRequestDTO requestDTO, Tuple2<StagingData, List<StagingAccountData>> stagingDataTuple) {
        AtomicReference<Boolean> isValid = new AtomicReference<>(true);
        if (this.validateSavingsAccountWithBalance(requestDTO.getData(), stagingDataTuple.getT2())) {
            for (AdjustedLoanData adjustedLoanData : requestDTO.getData()) {
                // isValid.set(this.validateLoanAccountWithAdjustedLoanData(adjustedLoanData,
                // stagingDataTuple.getT2()));
                if (!this.validateLoanAccountWithAdjustedLoanData(adjustedLoanData, stagingDataTuple.getT2())) {
                    isValid.set(false);
                    break;
                }
            }
        } else {
            return false;
        }
        return isValid.get();
    }

    private Boolean validateSavingsAccountWithBalance(List<AdjustedLoanData> adjustedLoanDataList, List<StagingAccountData> stagingAccountDataList) {
        AtomicReference<Boolean> isValid = new AtomicReference<>(true);
        // Get Available Balance For Savings Account
        Map<String, BigDecimal> savingsAccountWithAvailableBalance = new HashMap<>();
        stagingAccountDataList.forEach(stagingAccountData -> {
            if (!HelperUtil.checkIfNullOrEmpty(stagingAccountData.getSavingsAccountId())) {
                savingsAccountWithAvailableBalance.put(stagingAccountData.getSavingsAccountId(), stagingAccountData.getSavingsAvailableBalance());
            }

        });
        log.info("Savings Account List with Available Balance From Staging Data: {}",
                savingsAccountWithAvailableBalance);

        // Get Total Adjusted Amount From Requested Data Savings Account
        Map<String, BigDecimal> savingsAccountWithTotalAdjustedAmount = new HashMap<>();
        adjustedLoanDataList.forEach(adjustedLoanData -> {
            adjustedLoanData.getAdjustedAccountList().forEach(adjustedAccount -> {
                if (!savingsAccountWithTotalAdjustedAmount
                        .containsKey(adjustedAccount.getSavingsAccountId())) {
                    savingsAccountWithTotalAdjustedAmount.put(adjustedAccount.getSavingsAccountId(),
                            adjustedAccount.getAmount());
                } else {
                    savingsAccountWithTotalAdjustedAmount.replace(
                            adjustedAccount.getSavingsAccountId(),
                            adjustedAccount.getAmount().add(
                                    savingsAccountWithTotalAdjustedAmount.get(
                                            adjustedAccount.getSavingsAccountId())));
                }
            });
        });
        log.info("Savings Account List with Total Adjusted Amount From Request: {}",
                savingsAccountWithTotalAdjustedAmount);

        // Validate Savings Account Id and Available Balance with AdjustedAmount
        for (Map.Entry<String, BigDecimal> entry : savingsAccountWithTotalAdjustedAmount.entrySet()) {
            log.debug("Requested Savings Account: {} with Total Adjusted Amount: {}", entry.getKey(),
                    entry.getValue());
            if (!savingsAccountWithAvailableBalance.containsKey(entry.getKey())) {
                log.error("Savings Account Id does not Match");
                isValid.set(false);
            } else {
                if (savingsAccountWithAvailableBalance.get(entry.getKey())
                        .compareTo(entry.getValue()) < 0) {
                    log.error("Cannot Adjust More than Savings Available Balance");
                    isValid.set(false);
                }
            }

            if (!isValid.get()) {
                break;
            }
        }

        return isValid.get();
    }

    private Boolean validateLoanAccountWithAdjustedLoanData(AdjustedLoanData adjustedLoanData,
                                                            List<StagingAccountData> stagingAccountDataList) {

        StagingAccountData stagingAccountData = stagingAccountDataList.stream()
                .filter(data -> !HelperUtil.checkIfNullOrEmpty(data.getLoanAccountId())
                        && data.getLoanAccountId().equals(adjustedLoanData.getLoanAccountId()))
                .findFirst()
                .orElse(StagingAccountData.builder().build());

        if (HelperUtil.checkIfNullOrEmpty(stagingAccountData.getLoanAccountId())) {
            log.error("Loan Account Id does Not Match");
            return false;
        }

        BigDecimal maxPayableLoanAmount = stagingAccountData.getTotalPrincipalRemaining().add(stagingAccountData.getTotalServiceChargeRemaining());
        BigDecimal requestedRepayAmount = adjustedLoanData.getAdjustedAccountList().stream()
                .map(AdjustedAccount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Loan Account Id: {}, Max Payable Amount: {}, Requested Total Repay Amount: {}", adjustedLoanData.getLoanAccountId(), maxPayableLoanAmount, requestedRepayAmount);
        if (requestedRepayAmount.compareTo(maxPayableLoanAmount) > 0) {
            log.error("Requested Repay Amount cannot be more than Max Payable Amount");
            return false;
        }
        return true;
    }

    private net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.MemberInfoDTO extractMobileNumberFromMobileDetails(MemberEntity entity) {
        net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.MemberInfoDTO member = modelMapper.map(entity, net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.MemberInfoDTO.class);
        ArrayList mobileList = gson.fromJson(member.getMobile(), ArrayList.class);
        if (!mobileList.isEmpty()) {
            MobileInfoDTO mobileInfoDTO;
            try {
                mobileInfoDTO = gson.fromJson(mobileList.get(0).toString(), MobileInfoDTO.class);
            } catch (Exception e) {
                log.error("Error in parsing mobile info: {}", e.getMessage());
                mobileInfoDTO = new MobileInfoDTO();
            }
            member.setMobile(mobileInfoDTO.getContactNo());
        }
        return member;
    }

    @Override
    public Mono<AdjustmentEditResponseDto> updateAdjustmentAmount(AdjustmentEditRequestDto requestDto) {
        return port.getLoanAdjustmentCollectionDataByOid(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Adjustment request data not found.")))
                .flatMap(this::validateLoanRebateAndLoanWaiver)
                .filter(loanAdjustmentData -> !HelperUtil.checkIfNullOrEmpty(loanAdjustmentData.getStatus()) && (loanAdjustmentData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || loanAdjustmentData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Adjustment request data is not in staged or rejected state.")))
                .filter(loanAdjustmentData -> loanAdjustmentData.getCreatedBy().equalsIgnoreCase(requestDto.getLoginId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Adjustment request data can only be updated by the creator.")))
                .filter(data -> requestDto.getAdjustedAmount() != null && requestDto.getAdjustedAmount().compareTo(BigDecimal.ZERO) >= 0)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Adjusted amount must be positive!")))
                .filter(loanAdjustmentData -> validateAdjustmentAmount(requestDto))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Invalid adjustment amount.")))
                .flatMap(loanAdjustmentData -> validateLoanAmount(requestDto, loanAdjustmentData))
                .flatMap(adjustmentData -> port.getAllAdjustmentByManagementProcessIdAndLoanAdjustmentProcessId(adjustmentData.getManagementProcessId(), adjustmentData.getLoanAdjustmentProcessId())
                        .collectList())
                .flatMap(this::saveAdjustmentEditHistory)
                .flatMap(loanAdjustmentDataList -> validateAdjustedSavingsBalance(requestDto, loanAdjustmentDataList))
                .flatMapMany(Flux::fromIterable)
                .flatMap(loanAdjustmentData -> updateLoanAdjustmentData(requestDto, loanAdjustmentData))
                .collectList()
                .as(rxtx::transactional)
                .map(loanAdjustmentData -> AdjustmentEditResponseDto.builder().userMessage("Adjustment data updated successfully.").build());
    }

    private Mono<LoanAdjustmentData> validateLoanAmount(AdjustmentEditRequestDto requestDto, LoanAdjustmentData loanAdjustmentData) {
        return stagingDataUseCase.getStagingAccountDataByLoanAccountId(requestDto.getLoanAccountId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan account data not found.")))
                .zipWith(collectionStagingDataPersistencePort.getCollectionStagingDataByLoanAccountId(requestDto.getLoanAccountId())
                        .switchIfEmpty(Mono.just(CollectionStagingData.builder().amount(BigDecimal.ZERO).build())))
                .filter(tuple2 -> {
                    BigDecimal principalRemaining = tuple2.getT1().getTotalPrincipalRemaining() == null ? BigDecimal.ZERO : tuple2.getT1().getTotalPrincipalRemaining();
                    BigDecimal serviceChargeRemaining = tuple2.getT1().getTotalServiceChargeRemaining() == null ? BigDecimal.ZERO : tuple2.getT1().getTotalServiceChargeRemaining();
                    BigDecimal collectionAmount = tuple2.getT2().getAmount() == null ? BigDecimal.ZERO : tuple2.getT2().getAmount();
                    BigDecimal requestedAmount = requestDto.getAdjustedAmount() == null ? BigDecimal.ZERO : requestDto.getAdjustedAmount();
                    log.info("Principal Remaining: {}, Service Charge Remaining: {}, Requested Amount: {}", principalRemaining, serviceChargeRemaining, requestedAmount);
                    return principalRemaining.add(serviceChargeRemaining).subtract(collectionAmount).compareTo(requestedAmount) >= 0;
                })
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "adjusted amount can not exceed loan account outstanding..!")))
                .thenReturn(loanAdjustmentData);
    }

    private Mono<LoanAdjustmentData> validateLoanRebateAndLoanWaiver(LoanAdjustmentData loanAdjustmentData) {
        return loanRebatePersistencePort.getLoanRebateByLoanAccountId(loanAdjustmentData.getLoanAccountId())
                .filter(loanRebate -> loanRebate.getOid() != null)
                .flatMap(loanRebate -> Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account Already Collected By Loan Rebate")))
                .switchIfEmpty(loanWaiverPersistencePort.getLoanWaiverByLoanAccountId(loanAdjustmentData.getLoanAccountId())
                        .filter(loanWaiver -> loanWaiver.getOid() != null)
                        .flatMap(loanWaiver -> Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Account Already Collected By Loan Waiver")))
                        .switchIfEmpty(Mono.just(loanAdjustmentData)))
                .map(object -> loanAdjustmentData);
    }

    private Mono<LoanAdjustmentData> updateLoanAdjustmentData(AdjustmentEditRequestDto requestDto, LoanAdjustmentData loanAdjustmentData) {
        if (loanAdjustmentData.getLoanAccountId() == null && loanAdjustmentData.getSavingsAccountId() != null) {
            return port.saveEditedData(buildForSavingEditedData(requestDto, loanAdjustmentData));
        } else if (loanAdjustmentData.getLoanAccountId() != null && loanAdjustmentData.getSavingsAccountId() == null) {
            return port.saveEditedData(buildForLoanEditedData(requestDto, loanAdjustmentData));
        } else {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Invalid adjustment data."));
        }
    }

    private Mono<List<LoanAdjustmentData>> validateAdjustedSavingsBalance(AdjustmentEditRequestDto requestDto, List<LoanAdjustmentData> loanAdjustmentDataList) {
        List<Mono<Tuple3<StagingAccountData, CollectionStagingData, StagingWithdrawData>>> collect = loanAdjustmentDataList.stream()
                .filter(data -> !HelperUtil.checkIfNullOrEmpty(data.getSavingsAccountId()))
                .map(loanAdjustmentData -> getAccountDataCollectionDataWithdrawData(loanAdjustmentData)
                        .filter(tuple -> validateSavingsRequestedBalance(requestDto, loanAdjustmentData, tuple))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Insufficient balance for adjustment."))))
                .collect(Collectors.toList());
        return Mono.when(collect).thenReturn(loanAdjustmentDataList);
    }

    private Mono<Tuple3<StagingAccountData, CollectionStagingData, StagingWithdrawData>> getAccountDataCollectionDataWithdrawData(LoanAdjustmentData loanAdjustmentData) {
        return Mono.zip(
                stagingDataUseCase.getStagingAccountDataBySavingsAccountId(loanAdjustmentData.getSavingsAccountId())
                        .switchIfEmpty(Mono.just(StagingAccountData.builder().savingsAvailableBalance(BigDecimal.ZERO).build()))
                        .doOnRequest(l -> log.info("stagingAccountData Requested by : {}", loanAdjustmentData.getSavingsAccountId()))
                        .doOnNext(stagingAccountData -> log.info("stagingAccountData Response : {}", stagingAccountData)),
                collectionStagingDataPersistencePort.getCollectionStagingDataBySavingsAccountId(loanAdjustmentData.getSavingsAccountId())
                        .switchIfEmpty(Mono.just(CollectionStagingData.builder().amount(BigDecimal.ZERO).build()))
                        .doOnRequest(l -> log.info("collectionStagingData Requested by : {}", loanAdjustmentData.getSavingsAccountId()))
                        .doOnNext(collectionStagingData -> log.info("collectionStagingData Response : {}", collectionStagingData)),
                withdrawStagingDataPersistencePort.getWithdrawStagingDataBySavingsAccountId(loanAdjustmentData.getSavingsAccountId())
                        .switchIfEmpty(Mono.just(StagingWithdrawData.builder().amount(BigDecimal.ZERO).build()))
                        .doOnRequest(l -> log.info("stagingWithdrawData Requested by : {}", loanAdjustmentData.getSavingsAccountId()))
                        .doOnNext(stagingWithdrawData -> log.info("stagingWithdrawData Response : {}", stagingWithdrawData))
        );
    }

    private Mono<List<LoanAdjustmentData>> saveAdjustmentEditHistory(List<LoanAdjustmentData> loanAdjustmentDataList) {
        List<Mono<Void>> collect = loanAdjustmentDataList.stream()
                .map(loanAdjustmentData -> historyPort.saveAdjustmentEditHistory(buildLoanAdjustmentDataEditHistory(gson.fromJson(gson.toJson(loanAdjustmentData), LoanAdjustmentData.class)))
                        .doOnRequest(l -> log.info("Adjustment Edit History save to db for : {}", loanAdjustmentData.getOid()))
                        .then())
                .collect(Collectors.toList());
        return Mono.when(collect).thenReturn(loanAdjustmentDataList);
    }

    private LoanAdjustmentData buildForLoanEditedData(AdjustmentEditRequestDto requestDto, LoanAdjustmentData loanAdjustmentData) {
        loanAdjustmentData.setAmount(requestDto.getAdjustedAmount());
        loanAdjustmentData.setCurrentVersion(loanAdjustmentData.getCurrentVersion() + 1);
        loanAdjustmentData.setIsNew(NO.getValue());
        loanAdjustmentData.setUpdatedBy(requestDto.getLoginId());
        loanAdjustmentData.setUpdatedOn(LocalDateTime.now());
        return loanAdjustmentData;
    }

    private LoanAdjustmentData buildForSavingEditedData(AdjustmentEditRequestDto requestDto, LoanAdjustmentData loanAdjustmentData) {
        loanAdjustmentData.setAmount(requestedSavingsAmount(requestDto, loanAdjustmentData));
        loanAdjustmentData.setCurrentVersion(loanAdjustmentData.getCurrentVersion() + 1);
        loanAdjustmentData.setIsNew(NO.getValue());
        loanAdjustmentData.setUpdatedBy(requestDto.getLoginId());
        loanAdjustmentData.setUpdatedOn(LocalDateTime.now());
        return loanAdjustmentData;
    }

    private boolean validateSavingsRequestedBalance(AdjustmentEditRequestDto requestDto, LoanAdjustmentData loanAdjustmentData, Tuple3<StagingAccountData, CollectionStagingData, StagingWithdrawData> tuple) {
        log.info("requestAmount {} savingsAvailableBalance {} collectionStagingAmount {} withdrawalAmount {}", requestDto.getAdjustedAmount(), tuple.getT1().getSavingsAvailableBalance(), tuple.getT2().getAmount(), tuple.getT3().getAmount());
        BigDecimal totalSavingsAmount = requestedSavingsAmount(requestDto, loanAdjustmentData);
        BigDecimal savingsBalance = tuple.getT1().getSavingsAvailableBalance() == null ? BigDecimal.ZERO : tuple.getT1().getSavingsAvailableBalance();
        BigDecimal collectionStagingBalance = tuple.getT2().getAmount() == null ? BigDecimal.ZERO : tuple.getT2().getAmount();
        BigDecimal withdrawBalance = tuple.getT3().getAmount() == null ? BigDecimal.ZERO : tuple.getT3().getAmount();
        return totalSavingsAmount.compareTo(savingsBalance.add(collectionStagingBalance)) <= 0 &&
                savingsBalance.add(collectionStagingBalance).compareTo(withdrawBalance.add(totalSavingsAmount)) >= 0;
    }

    private static BigDecimal requestedSavingsAmount(AdjustmentEditRequestDto requestDto, LoanAdjustmentData loanAdjustmentData) {
        return requestDto.getAdjustedSavingsAccountList().stream()
                .filter(adjustedSavingsAccount -> adjustedSavingsAccount.getSavingsAccountId().equals(loanAdjustmentData.getSavingsAccountId()))
                .map(AdjustedSavingsAccount::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LoanAdjustmentDataEditHistory buildLoanAdjustmentDataEditHistory(LoanAdjustmentData loanAdjustmentData) {
        LoanAdjustmentDataEditHistory dataEditHistory = modelMapper.map(loanAdjustmentData, LoanAdjustmentDataEditHistory.class);
        dataEditHistory.setLoanAdjustmentDataId(loanAdjustmentData.getOid());
        dataEditHistory.setLoanAdjustmentDataEditHistoryId(UUID.randomUUID().toString());
        dataEditHistory.setOid(null);
        return dataEditHistory;
    }

    private boolean validateAdjustmentAmount(AdjustmentEditRequestDto request) {
        return request.getAdjustedAmount() != null &&
                request.getAdjustedAmount().compareTo(BigDecimal.ZERO) >= 0 &&
                request.getAdjustedSavingsAccountList().stream().map(AdjustedSavingsAccount::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add).equals(request.getAdjustedAmount());
    }

    @Override
    public Mono<LoanAdjustmentResponseDTO> submitAdjustmentDataEntity(AdjustmentEntitySubmitRequestDto requestDto) {
        return port.getLoanAdjustmentCollectionDataByOidList(requestDto.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Adjustment request data not found.")))
                .collectList()
                .filter(dataList -> dataList.size() == requestDto.getId().size())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Adjustment staging data not found.")))
                .flatMapMany(Flux::fromIterable)
                .flatMap(data -> {
                    if (!HelperUtil.checkIfNullOrEmpty(data.getCreatedBy()) && data.getCreatedBy().equalsIgnoreCase(requestDto.getLoginId()))
                        return Mono.just(data);
                    else
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Adjustment data can only be submitted by the creator!"));
                })
                .flatMap(data -> {
                    if (!HelperUtil.checkIfNullOrEmpty(data.getStatus()) && (data.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || data.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())))
                        return Mono.just(data);
                    else
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Data is already submitted or locked!"));
                })
                .flatMap(loanAdjustmentData -> this.getManagementProcessIdAndValidateSamityLoanAdjustmentDataForSubmission(requestDto.getOfficeId(), loanAdjustmentData.getSamityId())
                        .flatMap(managementProcessId -> port.updateStatusOfLoanAdjustmentDataForSubmission(requestDto, loanAdjustmentData)
                                .collectList()))
                .collectList()
                .map(lists -> LoanAdjustmentResponseDTO.builder()
                        .userMessage("Adjustment data submitted successfully.")
                        .build());
    }

    @Override
    public Mono<LoanAdjustmentResponseDTO> resetLoanAdjustmentDataByEntity(LoanAdjustmentRequestDTO requestDTO) {
        return port.getLoanAdjustmentCollectionDataByOid(requestDTO.getId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No adjustment data found for oid: " + requestDTO.getId())))
                .flatMap(loanAdjustmentData -> port.getAllAdjustmentByManagementProcessIdAndLoanAdjustmentProcessId(loanAdjustmentData.getManagementProcessId(), loanAdjustmentData.getLoanAdjustmentProcessId()).collectList())
                .filter(loanAdjustmentDataList -> loanAdjustmentDataList.stream().allMatch(loanAdjustmentData -> loanAdjustmentData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || loanAdjustmentData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Adjustment data is not eligible to reset!")))
                .flatMap(loanAdjustmentDataList -> port.deleteAllLoanAdjustmentDataByManagementProcessIdAndLoanAdjustmentProcessId(loanAdjustmentDataList.get(0).getManagementProcessId(), loanAdjustmentDataList.get(0).getProcessId())
                        .then(port.getLoanAdjustmentDataByManagementProcessIdAndSamity(loanAdjustmentDataList.get(0).getManagementProcessId(), loanAdjustmentDataList.get(0).getSamityId()).collectList())
                        .doOnNext(adjustList -> log.info("Adjustment data list after reset: {}", adjustList))
                        .filter(List::isEmpty)
                        .flatMap(adjustmentDataList -> samityEventTrackerUseCase.deleteSamityEventTrackerByEventList(loanAdjustmentDataList.get(0).getManagementProcessId(), loanAdjustmentDataList.get(0).getSamityId(), List.of(SamityEvents.LOAN_ADJUSTED.getValue())))
                )
                .as(rxtx::transactional)
                .doOnSuccess(responseDTO -> log.info("Adjustment data deleted successfully"))
                .thenReturn(LoanAdjustmentResponseDTO.builder()
                        .userMessage("Adjustment data deleted successfully.")
                        .build())
                .doOnError(throwable -> log.error("Error in delete adjustment data: {}", throwable.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), throwable -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.")));
    }

    @Override
    public Mono<List<LoanAdjustmentData>> getAllLoanAdjustmentDataByManagementProcessIdAndProcessId(String managementProcessId, String processId) {
        return port.getAllLoanAdjustmentDataByManagementProcessIdAndProcessId(managementProcessId, processId)
                .doOnSuccess(loanAdjustmentDataList -> log.info("Loan Adjustment Data List: {}", loanAdjustmentDataList));
    }

    @Override
    public Mono<LoanAdjustmentData> loanAdjustmentCollectionByLoanAccountId(String loanAccountId) {
        return port.getLoanAdjustmentCollectionDataByLoanAccountId(loanAccountId)
                .switchIfEmpty(Mono.just(LoanAdjustmentData.builder().build()))
                .doOnNext(loanAdjustmentData -> log.info("Request Received for Adjustment Collection by Loan Account Id : {}", loanAccountId))
                .doOnSuccess(loanAdjustmentData -> log.info("Loan Adjustment Data By Loan Account Id : {}", loanAdjustmentData))
                .doOnError(throwable -> log.error("Error in getting Loan Adjustment Data By Loan Account Id : {}", throwable.getMessage()));
    }

    @Override
    public Mono<Long> countLoanAdjustmentData(String managementProcessId, String samityId) {
        return port.getCountLoanAdjustmentByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .doOnSuccess(count -> log.info("Count of Loan Adjustment Data: {}", count));
    }
}
