package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.*;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.AccountingUseCase;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.AutoVoucherJournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.Journal;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.AutoVoucherUseCase;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.dto.AutoVoucherRequestDTO;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.CalendarUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.AccountWithProductEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberAndOfficeAndSamityEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.DayEndProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.request.DayEndProcessRequestDTO;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper.DayEndProcessProductTransaction;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper.DayEndProcessSamityResponse;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper.DayEndProcessTransaction;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper.ProductWithAccountId;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.domain.AccountingMetaProperty;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.domain.DayEndProcessTracker;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.FeeCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.FeeTypeSettingUseCase;
import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeCollection;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.WelfareFundUseCase;
import net.celloscope.mraims.loanportfolio.features.welfarefund.domain.WelfareFund;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.IWithdrawStagingDataUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple6;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DayEndProcessTrackerService implements DayEndProcessTrackerUseCase {

    private final DayEndProcessTrackerPersistencePort port;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;
    private final IStagingDataUseCase stagingDataUseCase;
    private final CollectionStagingDataQueryUseCase collectionStagingDataUseCase;
    private final IWithdrawStagingDataUseCase withdrawStagingDataUseCase;
    private final LoanAdjustmentUseCase loanAdjustmentUseCase;
    private final TransactionUseCase transactionUseCase;
    private final CommonRepository commonRepository;
    private final AccountingUseCase accountingUseCase;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final CalendarUseCase calendarUseCase;
    private final WelfareFundUseCase welfareFundUseCase;
    private final Gson gson;
    private final TransactionalOperator rxtx;
    private final AutoVoucherUseCase autoVoucherUseCase;
    private final FeeCollectionUseCase feeCollectionUseCase;
    private final PassbookUseCase passbookUseCase;
    private final MetaPropertyUseCase metaPropertyUseCase;
    private final FeeTypeSettingUseCase feeTypeSettingUseCase;

    public DayEndProcessTrackerService(DayEndProcessTrackerPersistencePort port, ManagementProcessTrackerUseCase managementProcessTrackerUseCase, OfficeEventTrackerUseCase officeEventTrackerUseCase, SamityEventTrackerUseCase samityEventTrackerUseCase, IStagingDataUseCase stagingDataUseCase, CollectionStagingDataQueryUseCase collectionStagingDataUseCase, IWithdrawStagingDataUseCase withdrawStagingDataUseCase, LoanAdjustmentUseCase loanAdjustmentUseCase, TransactionUseCase transactionUseCase, CommonRepository commonRepository, AccountingUseCase accountingUseCase, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, CalendarUseCase calendarUseCase, WelfareFundUseCase welfareFundUseCase, TransactionalOperator rxtx, AutoVoucherUseCase autoVoucherUseCase, FeeCollectionUseCase feeCollectionUseCase, PassbookUseCase passbookUseCase, MetaPropertyUseCase metaPropertyUseCase, FeeTypeSettingUseCase feeTypeSettingUseCase) {
        this.port = port;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.stagingDataUseCase = stagingDataUseCase;
        this.collectionStagingDataUseCase = collectionStagingDataUseCase;
        this.withdrawStagingDataUseCase = withdrawStagingDataUseCase;
        this.loanAdjustmentUseCase = loanAdjustmentUseCase;
        this.transactionUseCase = transactionUseCase;
        this.commonRepository = commonRepository;
        this.accountingUseCase = accountingUseCase;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.calendarUseCase = calendarUseCase;
        this.welfareFundUseCase = welfareFundUseCase;
        this.rxtx = rxtx;
        this.autoVoucherUseCase = autoVoucherUseCase;
        this.feeCollectionUseCase = feeCollectionUseCase;
        this.passbookUseCase = passbookUseCase;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.feeTypeSettingUseCase = feeTypeSettingUseCase;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<DayEndProcessGridViewResponseDTO> gridViewOfDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO) {
        final AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> this.getSamityResponseListForDayEndProcessForOffice(managementProcessTracker, requestDTO.getOfficeId())
                        .map(samityResponseList -> Tuples.of(managementProcessTracker, samityResponseList)))
                .map(tuples -> DayEndProcessGridViewResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(tuples.getT1().getOfficeNameEn())
                        .officeNameBn(tuples.getT1().getOfficeNameBn())
                        .businessDate(tuples.getT1().getBusinessDate())
                        .businessDay(tuples.getT1().getBusinessDay())
//                        .status(Status.STATUS_PENDING.getValue())
//                        .btnStartEnabled(tuples.getT2().isEmpty() || tuples.getT2().stream().allMatch(samityResponse -> samityResponse.getStatus().equals("Completed"))  ? "Yes" : "No")
                        .data(tuples.getT2())
                        .totalCount(tuples.getT2().size())
                        .build())
                .flatMap(dayEndProcessGridViewResponseDTO -> this.setBtnStatusForDayEndProcessGridView(managementProcess.get(), dayEndProcessGridViewResponseDTO))
                .flatMap(responseDTO -> this.setBtnStatusForWelfareFundOfDayEndProcessGridView(managementProcess.get().getManagementProcessId(), responseDTO))
                .flatMap(responseDTO -> this.getFinancialPeriodAvailability(responseDTO.getOfficeId())
                        .map(financialPeriodAvailability -> {
                            responseDTO.setIsFinancialPeriodAvailable(financialPeriodAvailability);
                            return responseDTO;
                        }))
                .doOnSuccess(response -> log.info("Day End Process Grid View Response: {}", response))
                .doOnError(throwable -> log.error("Error in Day End Process Grid View: {}", throwable.getMessage()));
    }

    private Mono<String> getFinancialPeriodAvailability(String officeId) {
        return commonRepository.getFinancialPeriodEntriesForOffice(officeId)
                .filter(financialPeriod -> financialPeriod.getStatus().equals(Status.STATUS_OPENED.getValue()))
                .collectList()
                .map(financialPeriodList -> financialPeriodList.isEmpty() ? "No" : "Yes");
    }

    private Mono<DayEndProcessGridViewResponseDTO> setBtnStatusForWelfareFundOfDayEndProcessGridView(String managementProcessId, DayEndProcessGridViewResponseDTO responseDTO) {
        return welfareFundUseCase.getAllWelfareFundTransactionForOfficeOnABusinessDay(managementProcessId, responseDTO.getOfficeId())
                .filter(welfareFund -> welfareFund.getStatus().equals(Status.STATUS_PENDING.getValue()))
                .collectList()
                .map(welfareFundList -> {
                    if (!welfareFundList.isEmpty()) {
                        responseDTO.setBtnStartEnabled("No");
                        responseDTO.setBtnDetailsEnabled("No");
                        responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                        responseDTO.setUserMessage("Welfare Fund Authorization is Pending For Office");
                    }
                    return responseDTO;
                });
    }

    private Mono<DayEndProcessGridViewResponseDTO> setBtnStatusForDayEndProcessGridView(ManagementProcessTracker managementProcessTracker, DayEndProcessGridViewResponseDTO responseDTO) {
        return this.getDayEndProcessEntriesAndOfficeEventsTuple(managementProcessTracker.getManagementProcessId(), responseDTO.getOfficeId())
                .map(tuples -> {
                    List<String> officeEventList = tuples.getT1();
                    log.info("Office Event List: {}", officeEventList);
                    List<DayEndProcessTracker> dayEndProcessTrackerList = tuples.getT2();
                    log.info("Day End Process Tracker List: {}", dayEndProcessTrackerList);
                    List<String> statusList = responseDTO.getData().stream().map(DayEndProcessSamityResponse::getStatus).toList();
                    log.info("Samity Event List: {}", statusList);

                    if (officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))) {
                        responseDTO.setBtnStartEnabled("No");
                        responseDTO.setBtnDetailsEnabled("Yes");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                        responseDTO.setUserMessage("Day End Process is Completed For Office");
                    } else if (officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue()))) {
                        responseDTO.setBtnStartEnabled("No");
                        responseDTO.setBtnDetailsEnabled("Yes");
                        responseDTO.setBtnDeleteEnabled("Yes");
                        responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                        responseDTO.setUserMessage("Auto Voucher Generation is Completed For Office");
                    } else if (officeEventList.stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))) {
                        responseDTO.setBtnStartEnabled("No");
                        responseDTO.setBtnDetailsEnabled("No");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                        responseDTO.setUserMessage("Staging Data Generation is Not Completed For Office, Auto Voucher Generation Cannot be Run");
                    } else if (!dayEndProcessTrackerList.isEmpty()) {
                        responseDTO.setBtnStartEnabled("No");
                        responseDTO.setBtnDetailsEnabled("Yes");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                        responseDTO.setUserMessage("Auto Voucher Generation is Running For Office");
                    } else {
                        if (!statusList.isEmpty() && responseDTO.getData().stream().map(DayEndProcessSamityResponse::getStatus).allMatch(samityStatus -> samityStatus.equals("Samity Canceled") || samityStatus.equals("Completed"))) {
                            responseDTO.setBtnStartEnabled("Yes");
                            responseDTO.setUserMessage("Auto Voucher Generation is Not Completed For Office");
                        } else {
                            responseDTO.setBtnStartEnabled("No");
                            responseDTO.setUserMessage("Transaction Authorization is Not Completed For Office");
                        }
                        responseDTO.setBtnDetailsEnabled("No");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_PENDING.getValue());

                    }
                    log.info("Response DTO: {}", responseDTO);
                    return responseDTO;
                });
    }

    private Mono<Tuple2<List<String>, List<DayEndProcessTracker>>> getDayEndProcessEntriesAndOfficeEventsTuple(String managementProcessId, String officeId) {
        return port.getDayEndProcessTrackerEntriesForOffice(managementProcessId, officeId)
                .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                .collectList()
                .flatMap(dayEndProcessTrackerList -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessId, officeId)
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList()
                        .map(officeEventList -> Tuples.of(officeEventList, dayEndProcessTrackerList)));
    }

    @Override
    public Mono<DayEndProcessDetailViewResponseDTO> detailViewOfDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(this::getAndBuildDayEndProcessDetailViewResponseFromTransaction)
                .flatMap(responseDTO -> this.getFinancialPeriodAvailability(responseDTO.getOfficeId())
                        .map(financialPeriodAvailability -> {
                            responseDTO.setIsFinancialPeriodAvailable(financialPeriodAvailability);
                            return responseDTO;
                        }))
                .flatMap(this::updateBtnStatusAndUserMessageForAutoVoucherGenerationOfDayEndProcessDetailView)
                .doOnNext(response -> log.info("Day End Process Detail View Response: {}", response))
                .doOnError(throwable -> log.error("Error in Day End Process Detail View: {}", throwable.getMessage()));
    }

    private Mono<DayEndProcessDetailViewResponseDTO> updateBtnStatusAndUserMessageForAutoVoucherGenerationOfDayEndProcessDetailView(DayEndProcessDetailViewResponseDTO responseDTO) {
        AtomicReference<String> managementProcessId = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(responseDTO.getOfficeId())
                .map(ManagementProcessTracker::getManagementProcessId)
                .doOnNext(managementProcessId::set)
                .flatMap(id -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessId.get(), responseDTO.getOfficeId())
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList())
                .flatMap(officeEventList -> port.getDayEndProcessTrackerEntriesForOffice(managementProcessId.get(), responseDTO.getOfficeId())
                        .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                        .collectList()
                        .map(dayEndProcessTrackerList -> Tuples.of(officeEventList, dayEndProcessTrackerList)))
                .map(tuples -> {
                    List<String> officeEventList = tuples.getT1();
                    List<String> dayEndProcessTrackerStatusList = tuples.getT2().stream().map(DayEndProcessTracker::getStatus).toList();

                    if (officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())) {
                        responseDTO.setBtnRefreshEnabled("No");
                        responseDTO.setBtnRetryEnabled("No");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                        responseDTO.setUserMessage("Day End Process is Completed For Office");
                    } else if (!dayEndProcessTrackerStatusList.isEmpty()) {
                        if (dayEndProcessTrackerStatusList.stream().allMatch(status -> status.equals(Status.STATUS_FINISHED.getValue()) || status.equals(Status.STATUS_FAILED.getValue()))) {
                            if (officeEventList.contains(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue())) {
                                responseDTO.setBtnRefreshEnabled("No");
                                responseDTO.setBtnRetryEnabled("No");
                                responseDTO.setBtnDeleteEnabled("Yes");
                                responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                                responseDTO.setUserMessage("Auto Voucher Generation is Completed For Office");
                            } else {
                                responseDTO.setBtnRefreshEnabled("Yes");
                                responseDTO.setBtnRetryEnabled("Yes");
                                responseDTO.setBtnDeleteEnabled("No");
                                responseDTO.setStatus(Status.STATUS_FAILED.getValue());
                                responseDTO.setUserMessage("Auto Voucher Generation is Failed For Office");
                            }
                        } else {
                            responseDTO.setBtnRefreshEnabled("Yes");
                            responseDTO.setBtnRetryEnabled("No");
                            responseDTO.setBtnDeleteEnabled("No");
                            responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                            responseDTO.setUserMessage("Auto Voucher Generation is Processing For Office");
                        }
                    } else if (!officeEventList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())) {
                        responseDTO.setBtnRefreshEnabled("No");
                        responseDTO.setBtnRetryEnabled("No");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.setUserMessage("Staging Data Generation is Not Completed For Office, Auto Voucher Generation Cannot be Run");
                    }
                    return responseDTO;
                });
    }

    @Override
    public Mono<DayEndProcessDetailViewResponseDTO> runDayEndProcessForOfficeV1(DayEndProcessRequestDTO requestDTO) {
        return this.validateIfDayEndProcessIsRunnable(requestDTO)
                .flatMap(managementProcessTracker -> this.buildAndRunDayEndProcessForOffice(managementProcessTracker, requestDTO.getLoginId()))
                .map(responseDTO -> {
                    responseDTO.setBtnRunDayEndProcessEnabled("No");
                    responseDTO.setBtnRefreshEnabled("Yes");
                    responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                    responseDTO.setUserMessage("Day End Process is Running For Office");
                    return responseDTO;
                })
                .doOnNext(response -> log.info("Run Day End Process Response: {}", response))
                .doOnError(throwable -> log.error("Error in Running Day End Process: {}", throwable.getMessage()));
    }

    @Override
    public Mono<DayEndProcessRetryResponseDTO> retryDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(managementProcessTracker -> this.validateIfDayEndProcessRetryRequestIsValid(managementProcessTracker, requestDTO)
                        .map(dayEndProcessTrackerList -> Tuples.of(managementProcessTracker, dayEndProcessTrackerList)))
                /*.doOnNext(tuples -> this.getAccountingAndUpdateDayEndProcessTrackerEntry(tuples.getT1(), tuples.getT2(), requestDTO.getLoginId(), tuples.getT2().get(0).getDayEndProcessTrackerId())
                        .subscribeOn(Schedulers.immediate())
                        .doOnError(throwable -> log.error("Error in Run Day End Process: {}", throwable.getMessage()))
                        .subscribe())*/
                .flatMap(tuples -> Mono.deferContextual(contextView -> {
                    // Pass the captured context to the background task
                    return Mono.fromRunnable(() -> {
                        // Restore the context in the background task
                        Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                        this.getAccountingAndUpdateDayEndProcessTrackerEntry(tuples.getT1(), tuples.getT2(), requestDTO.getLoginId(), tuples.getT2().get(0).getDayEndProcessTrackerId())
                                .contextWrite(context)
                                .subscribeOn(Schedulers.immediate())
                                .subscribe();
                    })
                            .thenReturn(tuples);
                }))
                .map(tuples -> DayEndProcessRetryResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(tuples.getT1().getOfficeNameEn())
                        .officeNameBn(tuples.getT1().getOfficeNameBn())
                        .businessDate(tuples.getT1().getBusinessDate())
                        .businessDay(tuples.getT1().getBusinessDay())
                        .userMessage("Day End Process Retry Started")
                        .transactionCodeList(tuples.getT2().stream().map(DayEndProcessTracker::getTransactionCode).toList())
                        .build())
                .doOnSuccess(response -> log.info("Day End Process Retry Response: {}", response))
                .doOnError(throwable -> log.error("Error in Day End Process retry: {}", throwable.getMessage()));
    }

    @Override
    public Mono<DayEndProcessRetryResponseDTO> retryAllDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(managementProcessTracker -> this.validateIfRetryAllForDayEndProcessRequestIsValid(managementProcessTracker, requestDTO)
                        .map(dayEndProcessTrackerList -> Tuples.of(managementProcessTracker, dayEndProcessTrackerList)))
                .flatMap(tuples -> Mono.deferContextual(contextView -> {
                    return Mono.fromRunnable(() -> {
                        Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                        this.getAccountingAndUpdateDayEndProcessTrackerEntry(tuples.getT1(), tuples.getT2(), requestDTO.getLoginId(), tuples.getT2().get(0).getDayEndProcessTrackerId())
                                .contextWrite(context)
                                .subscribeOn(Schedulers.immediate())
                                .subscribe();
                    })
                    .thenReturn(tuples);
                }))
                .map(tuples -> DayEndProcessRetryResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(tuples.getT1().getOfficeNameEn())
                        .officeNameBn(tuples.getT1().getOfficeNameBn())
                        .businessDate(tuples.getT1().getBusinessDate())
                        .businessDay(tuples.getT1().getBusinessDay())
                        .userMessage("Day End Process Retry Started")
                        .transactionCodeList(tuples.getT2().stream().map(DayEndProcessTracker::getTransactionCode).toList())
                        .build())
                .doOnSuccess(response -> log.info("Day End Process Retry Response: {}", response))
                .doOnError(throwable -> log.error("Error in Day End Process retry: {}", throwable.getMessage()));
    }

    @Override
    public Mono<DayEndProcessResponseDTO> generateAutoVoucherForOffice(DayEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(this::validateAutoVoucherGenerationProcessForOffice)
                .flatMap(managementProcessTracker -> Mono.deferContextual(contextView -> {
                    return Mono.fromRunnable(() -> {
                        Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                        this.generateAutoVoucherForOfficeDayEndProcess(managementProcessTracker, requestDTO.getLoginId())
                                .contextWrite(context)
                                .subscribeOn(Schedulers.immediate())
                                .subscribe();
                    })
                            .thenReturn(managementProcessTracker);
                }))
                .map(managementProcessTracker -> DayEndProcessResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .status(Status.STATUS_PROCESSING.getValue())
                        .userMessage("Day End Process Auto Voucher Generation Process is Started for Office")
                        .build())
                .doOnSuccess(response -> log.info("Run Day End Process Response status: {}, message: {}", response.getStatus(), response.getUserMessage()))
                .doOnError(throwable -> log.error("Error in Running Day End Process: {}", throwable.getMessage()));
    }

    @Override
    public Mono<DayEndProcessResponseDTO> retryAutoVoucherGenerationForOffice(DayEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> this.validateIfRetryForAutoVoucherGenerationForDayEndProcessRequestIsValid(managementProcessTracker, requestDTO))
                .flatMap(dayEndProcessTrackers -> Mono.deferContextual(contextView -> {
                    return Mono.fromRunnable(() -> {
                        Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                        this.retryAutoVoucherGeneration(managementProcess.get(), dayEndProcessTrackers, requestDTO.getOfficeId(), requestDTO.getLoginId())
                                .contextWrite(context)
                                .subscribeOn(Schedulers.immediate())
                                .subscribe();
                    })
                    .thenReturn(dayEndProcessTrackers);
                }))
                .map(tuples -> DayEndProcessResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .status(Status.STATUS_PROCESSING.getValue())
                        .userMessage("Auto Voucher Generation Retry Process is Started for Office")
                        .build())
                .doOnSuccess(response -> log.info("Auto Voucher Generation Retry Process Response: {}", response.getUserMessage()))
                .doOnError(throwable -> log.error("Error in Auto Voucher Generation Retry Process Response: {}", throwable.getMessage()));
    }

    @Override
    public Mono<DayEndProcessResponseDTO> deleteAutoVoucherGenerationForOffice(DayEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> {
                    if (requestDTO.getIsScheduledRequest()) {
                        return Mono.just(managementProcessTracker);
                    } else {
                        return validateIfAutoVoucherGenerationProcessDeletionIsValid(managementProcessTracker);
                    }
                })
                .flatMap(processTracker -> rxtx.transactional(this.deleteAutoVoucherGenerationDataForOffice(processTracker)))
                .map(data -> DayEndProcessResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .status(Status.STATUS_PENDING.getValue())
                        .userMessage("Auto Voucher Generation Process Data is Deleted for Office")
                        .build())
                .doOnSuccess(response -> log.info("Auto Voucher Generation Delete Process Response: {}", response.getUserMessage()))
                .doOnError(throwable -> log.error("Error in Auto Voucher Generation Delete Process Response: {}", throwable.getMessage()));
    }

    @Override
    public Mono<DayEndProcessResponseDTO> runDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(this::validateIfDayEndProcessIsRunnableForOffice)
                .flatMap(managementProcessTracker -> this.createAISJournalRequestAndRunDayEndProcessForOffice(managementProcessTracker, requestDTO.getLoginId()))
                .map(data -> DayEndProcessResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .status(Status.STATUS_FINISHED.getValue())
                        .userMessage("Day End Process is Completed Successfully")
                        .build())
                .doOnSuccess(response -> log.info("Run Day End Process Response: {}", response.getUserMessage()))
                /*.doOnSuccess(response -> loanRepaymentScheduleUseCase
                        .updateIsProvisionedStatus(Status.STATUS_PROCESSING.getValue(), Status.STATUS_YES.getValue())
                        .subscribeOn(Schedulers.boundedElastic()).subscribe())*/
                /*.doOnSuccess(response -> Mono.deferContextual(contextView -> {
                    // Pass the captured context to the background task
                    return Mono.fromRunnable(() -> {
                        // Restore the context in the background task
                        Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                        loanRepaymentScheduleUseCase.updateIsProvisionedStatus(Status.STATUS_PROCESSING.getValue(), Status.STATUS_YES.getValue())
                                .contextWrite(context)
                                .subscribeOn(Schedulers.boundedElastic()).subscribe();
                    })
                            .thenReturn(response);
                }))*/
                .doOnError(throwable -> log.error("Error Running Day End Process: {}", throwable.getMessage()));
    }

    @Override
    public Mono<DayEndProcessResponseDTO> deleteDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(this::validateIfDayEndProcessIsRevertableForOffice)
                .flatMap(managementProcessTracker -> this.RevertAISJournalRequestToDeleteDayEndProcessForOffice(managementProcessTracker, requestDTO.getLoginId()))
                .map(data -> DayEndProcessResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .status(Status.STATUS_PENDING.getValue())
                        .userMessage("Day End Process is Reverted Successfully")
                        .build())
                .doOnSuccess(response -> log.info("Delete Day End Process Response: {}", response.getUserMessage()))
                .doOnError(throwable -> log.error("Error Deleting Day End Process: {}", throwable.getMessage()));
    }

    private Mono<ManagementProcessTracker> validateIfDayEndProcessIsRevertableForOffice(ManagementProcessTracker processTracker) {
        log.info("Validating if Day End Process is Revertable for Office: {}", processTracker.getOfficeId());
        return officeEventTrackerUseCase.getAllOfficeEventsForOffice(processTracker.getManagementProcessId(), processTracker.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .map(OfficeEventTracker::getOfficeEvent)
                .collectList()
                .doOnNext(officeEventList -> log.info("Office Event List for Reverting Day End Process for Office {} is: {}", processTracker.getOfficeId(), officeEventList))
                .filter(officeEventList -> officeEventList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                .filter(officeEventList -> officeEventList.contains(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Auto Voucher Generation Process is Not Completed For Office")))
                .filter(officeEventList -> officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Not Completed for Office")))
                .filter(officeEventList -> !officeEventList.contains(OfficeEvents.FORWARD_DAY_ROUTINE_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Forward Day Routine is Completed for Office")))
                .map(officeEventList -> processTracker)
                .doOnNext(response -> log.info("Day End Process is Revertable for Office: {}", processTracker.getOfficeId()));
    }

    private Mono<ManagementProcessTracker> RevertAISJournalRequestToDeleteDayEndProcessForOffice(ManagementProcessTracker managementProcessTracker, String loginId) {
        log.info("Reverting AIS Journal Request to Delete Day End Process for Office: {}", managementProcessTracker.getOfficeId());
        return /*this.getAutoVoucherProcessIdForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .flatMap(autoVoucherProcessId -> */
                autoVoucherUseCase.updateAutoVoucherAndVoucherDetailStatus(managementProcessTracker.getManagementProcessId(), loginId, Status.STATUS_PENDING.getValue())
                .flatMap(response -> officeEventTrackerUseCase.deleteOfficeEventForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())
                        .then(Mono.fromCallable(() -> managementProcessTracker)))
                .doOnNext(response -> log.info("AIS Journal Request is Reverted and Office Event is Deleted For Day End Process of Office: {}", managementProcessTracker.getOfficeId()));
    }

    private Mono<DayEndProcessResponseDTO> deleteDayEndProcess(DayEndProcessRequestDTO requestDTO) {
        return managementProcessTrackerUseCase
                .getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(managementProcessTracker ->
                        port.deleteDayEndProcessTrackerEntryListForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                )
                .as(this.rxtx::transactional)
                .map(data -> DayEndProcessResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .status(Status.STATUS_COMPLETED.getValue())
                        .userMessage("Day End Process has been executed Successfully")
                        .build())
                .doOnSuccess(response -> log.info("Delete Day End Process Response: {}", response.getUserMessage()))
                .doOnError(throwable -> log.error("Error Deleting Day End Process: {}", throwable.getMessage()));
    }

    @Override
    public Mono<DayEndProcessResponseDTO> revertDayEndProcessByAISForOffice(DayEndProcessRequestDTO requestDTO) {
        return managementProcessTrackerUseCase
                .getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(this::validateIfDayEndProcessIsRevertibleByAISForOffice)
                .flatMap(managementProcessTracker -> this.revertAISDayEndProcessSteps(managementProcessTracker, requestDTO.getLoginId()))
                .as(this.rxtx::transactional)
                .map(data -> DayEndProcessResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .status(Status.STATUS_COMPLETED.getValue())
                        .userMessage("Day End Process is Reverted Successfully")
                        .build())
                .doOnSuccess(response -> log.info("Revert Day End Process Response: {}", response.getUserMessage()))
                .doOnError(throwable -> log.error("Error Reverting Day End Process: {}", throwable.getMessage()));
    }

    @Override
    public Mono<DayEndProcessResponseDTO> revertDayEndProcessByMISForOffice(DayEndProcessRequestDTO requestDTO) {
        return managementProcessTrackerUseCase
                .getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(this::validateIfDayEndProcessIsRevertibleByMISForOffice)
                .flatMap(managementProcessTracker -> this.revertMISDayEndProcessSteps(managementProcessTracker, requestDTO.getMfiId(), requestDTO.getOfficeId()))
                .as(this.rxtx::transactional)
                .map(data -> DayEndProcessResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .status(Status.STATUS_COMPLETED.getValue())
                        .userMessage("Day End Process is Reverted Successfully")
                        .build())
                .doOnSuccess(response -> log.info("Revert MIS Day End Process Response: {}", response.getUserMessage()))
                .doOnError(throwable -> log.error("Error Reverting MIS Day End Process: {}", throwable.getMessage()));
    }

    private Mono<ManagementProcessTracker> revertAISDayEndProcessSteps(ManagementProcessTracker managementProcessTracker, String loginId) {
        log.info("Reverting AIS Day End Process for Office: {}", managementProcessTracker.getOfficeId());
        return /*this
                .getAutoVoucherProcessIdForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .flatMap(autoVoucherProcessid -> */
                this.revertAccountingJournal(managementProcessTracker.getManagementProcessId())
                        /*.switchIfEmpty(Mono.just(autoVoucherProcessid)))*/
                .flatMap(managementProcessId ->
//                        insertDataIntoAutoVoucherHistoryAndAutoVoucherDetailHistory(managementProcessTracker.getManagementProcessId(), autoVoucherProcessId)
//                                .then(
                                autoVoucherUseCase
                                        .updateAutoVoucherAndVoucherDetailStatus(managementProcessTracker.getManagementProcessId(), loginId, Status.STATUS_PENDING.getValue())
//                                )
                )
                .flatMap(response -> updateDayEndProcessTrackerSettingNullValueInAISResponseForAisDayEndProcess(managementProcessTracker))
                .flatMap(response ->
                        deleteOfficeEventTracker(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())
                                .then(Mono.fromCallable(() -> managementProcessTracker))
                )
                .doOnNext(response -> log.info("AIS Day End Process Revert Completed"));
    }


    private Mono<String> revertAccountingJournal(String managementProcessId) {
        return rxtx.transactional(
                autoVoucherUseCase.getAutoVoucherListByManagementProcessId(managementProcessId)
                        .flatMapMany(Flux::fromIterable)
                        .filter(autoVoucher -> Strings.isNotNullAndNotEmpty(autoVoucher.getAisRequest()))
                        .flatMap(autoVoucher -> {
                            JournalRequestDTO journalRequestDTO = gson.fromJson(autoVoucher.getAisRequest(), JournalRequestDTO.class);
                            return Mono.just(this.reverseJournalRequestDTO(journalRequestDTO))
                                    .flatMap(reversedJournalDTO -> accountingUseCase.saveAccountingJournal(reversedJournalDTO)
                                            .doOnSuccess(response -> log.info("Accounting Journal Reverted Successfully for : {}", autoVoucher.getVoucherId()))
                                            .flatMap(response -> autoVoucherUseCase.updateAutoVoucherWithAisRequest(autoVoucher.getOid(), reversedJournalDTO.toString())));
                        })
                        .collectList()
                        .map(aisResponses -> managementProcessId));
    }

    private JournalRequestDTO reverseJournalRequestDTO(JournalRequestDTO requestDTO) {
        log.info("Reversing Journal Request: {}", requestDTO);
        requestDTO.getJournalList()
                .forEach(journal -> {
                    BigDecimal debitedAmount = journal.getDebitedAmount();
                    BigDecimal creditedAmount = journal.getCreditedAmount();
                    journal.setDebitedAmount(creditedAmount);
                    journal.setCreditedAmount(debitedAmount);
                    journal.setDescription("REVERSED " + journal.getDescription());
                });

        requestDTO.setDescription("REVERSED " + requestDTO.getDescription());
        requestDTO.setProcessId("REVERSED " + requestDTO.getProcessId());

        log.info("Reversed Journal Request: {}", requestDTO);
        return requestDTO;
    }

    private Mono<Boolean> updateDayEndProcessTrackerSettingNullValueInAISResponseForAisDayEndProcess(ManagementProcessTracker managementProcessTracker) {
//        return port
//                .getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
//                .flatMap(dayEndProcessTracker ->
//                        port.saveAISDayEndProcessTrackerIntoHistory(dayEndProcessTracker)
//                                .doOnSuccess(response -> log.info("Day End Process Tracker Entry is Saved in History for AIS day end"))
//                                .doOnError(throwable -> log.error("Error Saving Day End Process Tracker Entry in History for AIS day end: {}", throwable.getMessage()))
//                                .then(port.updateDayEndProcessTrackerEntryAisResponse(dayEndProcessTracker, null))
//                                .doOnSuccess(response -> log.info("Day End Process Tracker Entry is Updated for AIS day end"))
//                                .doOnError(throwable -> log.error("Error in Updating Day End Process Tracker Entry for AIS day end: {}", throwable.getMessage()))
//                )
//                .then(Mono.just(Boolean.TRUE));
        return port
                .getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .flatMap(dayEndProcessTracker -> port.updateDayEndProcessTrackerEntryAisResponse(dayEndProcessTracker, null)
                        .doOnSuccess(response -> log.info("Day End Process Tracker Entry is Updated for AIS day end"))
                        .doOnError(throwable -> log.error("Error in Updating Day End Process Tracker Entry for AIS day end: {}", throwable.getMessage()))
                )
                .then(Mono.just(Boolean.TRUE));
    }

    private Mono<ManagementProcessTracker> revertMISDayEndProcessSteps(ManagementProcessTracker managementProcessTracker, String mfiId, String officeId) {
        log.info("Reverting MIS Day End Process for Office: {}", managementProcessTracker.getOfficeId());
        return this
                .getAutoVoucherProcessIdForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .flatMap(autoVoucherProcessId ->
//                        insertDataIntoAutoVoucherHistoryAndAutoVoucherDetailHistory(managementProcessTracker.getManagementProcessId(), autoVoucherProcessId)
//                                .then(
                                /*autoVoucherUseCase
                                        .deleteAutoVoucherListByManagementProcessIdAndProcessId(managementProcessTracker.getManagementProcessId(), autoVoucherProcessId)*/
//                                )
                        autoVoucherUseCase.deleteAutoVoucherListByManagementProcessId(managementProcessTracker.getManagementProcessId())
                )
                .flatMap(s -> feeCollectionUseCase.rollbackFeeCollectionOnMISDayEndRevert(officeId, managementProcessTracker.getManagementProcessId())
                            .thenReturn(s))
                .flatMap(aBoolean ->
                        deleteDayEndProcessTrackerForMISDayEndProcess(managementProcessTracker)
                )
                .flatMap(response ->
                        deleteOfficeEventTracker(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue())
                                .then(Mono.fromCallable(() -> managementProcessTracker))
                )
                .doOnNext(response -> log.info("MIS Day End Process Completed"));
    }

    private Mono<Boolean> deleteDayEndProcessTrackerForMISDayEndProcess(ManagementProcessTracker managementProcessTracker) {
//        return port
//                .getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
//                .flatMap(dayEndProcessTracker ->
//                        port.saveMISDayEndProcessTrackerIntoHistory(dayEndProcessTracker)
//                                .doOnSuccess(response -> log.info("Day End Process Tracker Entry is Saved in History for MIS day end"))
//                                .doOnError(throwable -> log.error("Error Saving Day End Process Tracker Entry in History for MIS day end: {}", throwable.getMessage()))
//                                .then(port.deleteDayEndProcessTrackerEntryListForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId()))
//                                .doOnSuccess(response -> log.info("Day End Process Tracker Entry is deleted for MIS day end"))
//                                .doOnError(throwable -> log.error("Error in Deleting Day End Process Tracker Entry for MIS day end: {}", throwable.getMessage()))
//                )
//                .then(Mono.just(Boolean.TRUE));
        return port.deleteDayEndProcessTrackerEntryListForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .doOnSuccess(response -> log.info("Day End Process Tracker Entry is deleted for MIS day end"))
                .doOnError(throwable -> log.error("Error in Deleting Day End Process Tracker Entry for MIS day end: {}", throwable.getMessage()))
                .then(Mono.just(Boolean.TRUE));
    }

    private Mono<Boolean> deleteOfficeEventTracker(String managementProcessId, String officeId, String officeEvent) {
        return officeEventTrackerUseCase
                .deleteOfficeEventForOffice(managementProcessId, officeId, officeEvent)
                .doOnRequest(value -> log.info("Request received for Archiving Office Event Tracker and Deleting Office Event"))
                .doOnSuccess(value -> log.info("Office Event Tracker Archived and Office Event Deleted"))
                .doOnError(throwable -> log.error("Error in Archiving Office Event Tracker and Deleting Office Event: {}", throwable.getMessage()))
                .thenReturn(Boolean.TRUE)
                ;
    }

    private Mono<Boolean> insertDataIntoAutoVoucherHistoryAndAutoVoucherDetailHistory(String managementProcessId, String processId) {
        return autoVoucherUseCase
                .saveAutoVoucherHistoryAndVoucherDetailHistoryForArchiving(managementProcessId, processId)
                .doOnRequest(value -> log.info("Request received for Inserting Data into Auto Voucher History and Auto Voucher Detail History"))
                .doOnSuccess(value -> log.info("Data Inserted into Auto Voucher History and Auto Voucher Detail History"))
                .doOnError(throwable -> log.error("Error in Inserting Data into Auto Voucher History and Auto Voucher Detail History: {}", throwable.getMessage()))
                .thenReturn(Boolean.TRUE)
                ;
    }

    private Mono<ManagementProcessTracker> validateIfDayEndProcessIsRevertibleByAISForOffice(ManagementProcessTracker processTracker) {
        log.info("Validating if AIS Day End Process is Revertible for Office: {}", processTracker.getOfficeId());
        return officeEventTrackerUseCase
                .getLastOfficeEventForOffice(processTracker.getManagementProcessId(), processTracker.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .flatMap(officeEventTracker -> {
                            if (officeEventTracker.getOfficeEvent().equalsIgnoreCase(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue())) {
                                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process Has Not Been Reverted for Office"));
                            } else if (!officeEventTracker.getOfficeEvent().equalsIgnoreCase(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())) {
                                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Not Completed for Office"));
                            }
                            return Mono.just(processTracker);
                        }
                );
    }

    private Mono<ManagementProcessTracker> validateIfDayEndProcessIsRevertibleByMISForOffice(ManagementProcessTracker processTracker) {
        log.info("Validating if MIS Day End Process is Revertible for Office: {}", processTracker.getOfficeId());
        return officeEventTrackerUseCase
                .getLastOfficeEventForOffice(processTracker.getManagementProcessId(), processTracker.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .flatMap(officeEventTracker -> {
                            if (officeEventTracker.getOfficeEvent().equalsIgnoreCase(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())) {
                                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process Has Not Been Reverted for Office"));
                            } else if (!officeEventTracker.getOfficeEvent().equalsIgnoreCase(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue())) {
                                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Auto Voucher Generation Process is Not Completed For Office"));
                            }
                            return Mono.just(processTracker);
                        }
                );
    }

    @Override
    public Mono<DayEndProcessStatusResponseDTO> getStatusOfDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO) {
        AtomicReference<String> managementProcessId = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess -> managementProcessId.set(managementProcess.getManagementProcessId()))
                .map(managementProcessTracker -> DayEndProcessStatusResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcessTracker.getOfficeNameEn())
                        .officeNameBn(managementProcessTracker.getOfficeNameBn())
                        .businessDate(managementProcessTracker.getBusinessDate())
                        .businessDay(managementProcessTracker.getBusinessDay())
                        .build())
                .flatMap(responseDTO -> this.setBtnStatusForDayEndProcessStatusResponse(managementProcessId.get(), responseDTO))
                .doOnSuccess(response -> log.info("Day End Process Status Response: {}", response))
                .doOnError(throwable -> log.error("Error in Day End Process Status Response: {}", throwable.getMessage()));
    }

    private Mono<DayEndProcessStatusResponseDTO> setBtnStatusForDayEndProcessStatusResponse(String managementProcessId, DayEndProcessStatusResponseDTO responseDTO) {
        return officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessId, responseDTO.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .map(OfficeEventTracker::getOfficeEvent)
                .collectList()
                .doOnNext(officeEventList -> log.info("Office Event List for Day End Process Status of Office: {} is: {}", responseDTO.getOfficeId(), officeEventList))
                .map(officeEventList -> {
                    if (officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())) {
                        responseDTO.setBtnRunDayEndProcessEnabled("No");
                        responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                        if (officeEventList.contains(OfficeEvents.FORWARD_DAY_ROUTINE_COMPLETED.getValue())) {
                            responseDTO.setBtnDeleteEnabled("No");
                            responseDTO.setUserMessage("Forward Day Routine is Completed For Office");
                        } else {
                            responseDTO.setBtnDeleteEnabled("Yes");
                            responseDTO.setUserMessage("Day End Process is Completed For Office");
                        }
                    } else if (officeEventList.contains(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue())) {
                        responseDTO.setBtnRunDayEndProcessEnabled("Yes");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                        responseDTO.setUserMessage("Day End Process is Not Completed For Office");
                    } else {
                        responseDTO.setBtnRunDayEndProcessEnabled("No");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.setUserMessage("Day End Process Cannot be Run For Office, Auto Voucher Generation Process is Not Completed");
                    }
                    return responseDTO;
                });
    }

    private Mono<ManagementProcessTracker> createAISJournalRequestAndRunDayEndProcessForOffice(ManagementProcessTracker managementProcessTracker, String loginId) {
        log.info("Creating AIS Journal Request and Running Day End Process for Office: {}", managementProcessTracker.getOfficeId());
        final String dayEndProcessId = UUID.randomUUID().toString();
        return /*this.getAutoVoucherProcessIdForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .flatMap(autoVoucherProcessId -> */
                accountingUseCase.buildAndSaveAccountingJournalFromAutoVoucherList(AutoVoucherJournalRequestDTO.builder()
                        .managementProcessId(managementProcessTracker.getManagementProcessId())
//                        .processId(autoVoucherProcessId)
                        .officeId(managementProcessTracker.getOfficeId())
                        .mfiId(managementProcessTracker.getMfiId())
                        .loginId(loginId)
                        .businessDate(managementProcessTracker.getBusinessDate())
                        .build())
                .flatMap(response -> /*this.getAutoVoucherProcessIdForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .flatMap(processId -> */
                        autoVoucherUseCase.updateAutoVoucherAndVoucherDetailStatus(managementProcessTracker.getManagementProcessId(), loginId, Status.STATUS_APPROVED.getValue())
                                .flatMap(aBoolean -> {
                                    if (!aBoolean) {
                                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Updating Auto Voucher Status Failed"));
                                    }
                                    return Mono.just(response);
                                }))
                .flatMap(response -> officeEventTrackerUseCase.insertOfficeEvent(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue(), loginId, dayEndProcessId)
                        .map(officeEventTracker -> managementProcessTracker))
                .doOnNext(response -> log.info("AIS Journal Created, Voucher Status Updated And Office Event Inserted for Day End Process of Office: {}", managementProcessTracker.getOfficeId()));
    }

    private Mono<ManagementProcessTracker> validateIfDayEndProcessIsRunnableForOffice(ManagementProcessTracker processTracker) {
        log.info("Validating if Day End Process is Runnable for Office: {}", processTracker.getOfficeId());
        return officeEventTrackerUseCase.getAllOfficeEventsForOffice(processTracker.getManagementProcessId(), processTracker.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .map(OfficeEventTracker::getOfficeEvent)
                .collectList()
                .doOnNext(officeEventList -> log.info("Office Event List for Day End Process for Office: {} is: {}", processTracker.getOfficeId(), officeEventList))
                .filter(officeEventList -> officeEventList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                .filter(officeEventList -> officeEventList.contains(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Auto Voucher Generation Process is Not Completed For Office")))
                .filter(officeEventList -> !officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed for Office")))
                .map(officeEventList -> processTracker)
                .doOnNext(response -> log.info("Day End Process is Runnable for Office: {}", processTracker.getOfficeId()));
    }

    private Mono<ManagementProcessTracker> deleteAutoVoucherGenerationDataForOffice(ManagementProcessTracker processTracker) {
        log.info("Deleting Auto Voucher Generation Data for Office: {}", processTracker.getOfficeId());
        return this.getAutoVoucherProcessIdForOffice(processTracker.getManagementProcessId(), processTracker.getOfficeId())
                .flatMap(processId -> port.deleteDayEndProcessTrackerEntryListForOffice(processTracker.getManagementProcessId(), processTracker.getOfficeId())
                        .doOnNext(response -> log.info("Day End Process Tracker Entry is Deleted for Office: {}", processTracker.getOfficeId()))
                        .map(response -> processId))
                /*.flatMap(processId -> this.deleteAutoVoucherEntryForOffice(processTracker.getManagementProcessId(), processTracker.getOfficeId(), processId)
                        .doOnNext(response -> log.info("Auto Voucher Entry is Deleted for Office: {}", processTracker.getOfficeId()))
                        .map(response -> processId))*/
                .flatMap(processId -> this.deleteAutoVoucherEntryForOfficeByManagementProcessId(processTracker.getManagementProcessId())
                        .doOnNext(response -> log.info("Auto Voucher Entry is Deleted for Office: {}", processTracker.getOfficeId()))
                        .map(response -> processId))
                .flatMap(processId -> officeEventTrackerUseCase.deleteOfficeEventForOffice(processTracker.getManagementProcessId(), processTracker.getOfficeId(), OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue())
                        .then(Mono.fromCallable(() -> processTracker)))
                .doOnNext(response -> log.info("Auto Voucher Generation Data is Deleted for Office: {}", processTracker.getOfficeId()));
    }

    private Mono<String> getAutoVoucherProcessIdForOffice(String managementProcessId, String officeId) {
        return port.getDayEndProcessTrackerEntriesForOffice(managementProcessId, officeId)
                .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                .map(DayEndProcessTracker::getDayEndProcessTrackerId)
                .next();
    }

    private Mono<String> deleteAutoVoucherEntryForOffice(String managementProcessId, String officeId, String processId) {
        return autoVoucherUseCase.deleteAutoVoucherListByManagementProcessIdAndProcessId(managementProcessId, processId)
                .map(aBoolean -> processId);
    }

    private Mono<String> deleteAutoVoucherEntryForOfficeByManagementProcessId(String managementProcessId) {
        return autoVoucherUseCase.deleteAutoVoucherListByManagementProcessId(managementProcessId)
                .map(aBoolean -> managementProcessId);
    }

    private Mono<ManagementProcessTracker> validateIfAutoVoucherGenerationProcessDeletionIsValid(ManagementProcessTracker managementProcessTracker) {
        log.info("Validating Auto Voucher Generation Process Deletion for Office: {}", managementProcessTracker.getOfficeId());
        return this.validateOfficeEventListForAutoVoucherDeletionProcess(managementProcessTracker)
                .flatMap(this::validateDayEndProcessTrackerEntriesForAutoVoucherDeletion)
                .doOnNext(processTracker -> log.info("Auto Voucher Generation Process Deletion is Valid for Office: {}", managementProcessTracker.getOfficeId()));
    }

    private Mono<ManagementProcessTracker> validateDayEndProcessTrackerEntriesForAutoVoucherDeletion(ManagementProcessTracker managementProcessTracker) {
        log.info("Validating Day End Process Entry List for Auto Voucher Deletion Process for Office: {}", managementProcessTracker.getOfficeId());
        return port.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                .collectList()
                .filter(dayEndProcessTrackerList -> !dayEndProcessTrackerList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Auto Voucher Generation Process is not Started for Office")))
                .filter(dayEndProcessTrackerList -> dayEndProcessTrackerList.stream().allMatch(dayEndProcessTracker -> dayEndProcessTracker.getStatus().equals(Status.STATUS_FINISHED.getValue()) || dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Auto Voucher Generation Process is Running for Office")))
                .map(dayEndProcessTrackerList -> managementProcessTracker)
                .doOnNext(response -> log.info("Day End Process Entry List for Auto Voucher Deletion is Valid for Office: {}", managementProcessTracker.getOfficeId()));
    }

    private Mono<ManagementProcessTracker> validateOfficeEventListForAutoVoucherDeletionProcess(ManagementProcessTracker managementProcessTracker) {
        log.info("Validating Office Event List for Auto Voucher Deletion Process for Office: {}", managementProcessTracker.getOfficeId());
        return officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .map(OfficeEventTracker::getOfficeEvent)
                .collectList()
                .doOnNext(officeEventList -> log.info("Office Event List for Auto Voucher Deletion for Office: {}", officeEventList))
                .filter(officeEventList -> officeEventList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                .filter(officeEventList -> !officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed for Office")))
                .map(officeEventList -> managementProcessTracker)
                .doOnNext(processTracker -> log.info("Office Event List for Auto Voucher Deletion is Valid for Office: {}", managementProcessTracker.getOfficeId()));
    }

    private Mono<List<DayEndProcessTracker>> validateIfRetryForAutoVoucherGenerationForDayEndProcessRequestIsValid(ManagementProcessTracker managementProcessTracker, DayEndProcessRequestDTO requestDTO) {
        log.info("Validating Auto Voucher Generation Process Retry Request for Office: {}", managementProcessTracker.getOfficeId());
        return officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .map(OfficeEventTracker::getOfficeEvent)
                .collectList()
                .flatMap(officeEventlist -> port.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                        .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                        .collectList()
                        .map(processTrackerList -> Tuples.of(officeEventlist, processTrackerList)))
                .doOnNext(tuples -> log.info("Auto Voucher Generation Process transaction Code List to Retry: {}", tuples.getT2().stream().map(DayEndProcessTracker::getTransactionCode).toList()))
                .flatMap(tuples -> {
                    List<String> officeEventlist = tuples.getT1();
                    List<DayEndProcessTracker> dayEndProcessTrackerList = tuples.getT2();
                    if (officeEventlist.contains(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue())) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Auto Voucher Generation Process Retry Request is Invalid"));
                    }
                    return Mono.just(dayEndProcessTrackerList);
                })
                .doOnNext(response -> log.info("Auto Voucher Generation Process Retry Request is Valid for Office: {}", managementProcessTracker.getOfficeId()));
    }

    private Mono<Void> retryAutoVoucherGeneration(ManagementProcessTracker managementProcessTracker, List<DayEndProcessTracker> dayEndProcessTrackerList, String officeId, String loginId) {
        return port.updateDayEndProcessEntryListForRetry(dayEndProcessTrackerList, loginId)
                .doOnError(throwable -> log.error("Error in Updating Day End Process Entry List for Retry: {}", throwable.getMessage()))
                .flatMap(dayEndProcessTrackers -> this.getAccountingAndCreateAutoVoucherEntryForOfficeAndUpdateOfficeEventTracker(managementProcessTracker, dayEndProcessTrackers, loginId))
                .doOnSuccess(response -> log.info("Auto Voucher Generation Process Retry is completed for office: {}", officeId))
                .doOnError(throwable -> log.error("Error in Auto Voucher Generation Process Retry: {}", throwable.getMessage()))
               /* .subscribeOn(Schedulers.immediate())
                .subscribe();*/
                .then();
    }

    private Mono<ManagementProcessTracker> validateAutoVoucherGenerationProcessForOffice(ManagementProcessTracker managementProcessTracker) {
        log.info("Validating Auto Voucher Generation Process for Office: {}", managementProcessTracker.getOfficeId());
        return this.validateOfficeEventListForAutoVoucherGeneration(managementProcessTracker)
                .flatMap(this::checkIfAutoVoucherGenerationProcessIsAlreadyRunningForOffice)
                .flatMap(this::validateSamityListForAutoVoucherGeneration)
                .flatMap(this::validateWelfareFundAuthorizationForAutoVoucherGeneration)
                .flatMap(this::validatePenaltyAuthorizationForAutoVoucherGeneration)
                .doOnNext(response -> log.info("Auto Voucher Generation Process is Valid for Office: {}", managementProcessTracker.getOfficeId()));
    }


    private Mono<ManagementProcessTracker> validatePenaltyAuthorizationForAutoVoucherGeneration(ManagementProcessTracker managementProcessTracker) {
        return Mono.just(managementProcessTracker);
    }

    private Mono<ManagementProcessTracker> validateWelfareFundAuthorizationForAutoVoucherGeneration(ManagementProcessTracker managementProcessTracker) {
        return welfareFundUseCase.getAllWelfareFundTransactionForOfficeOnABusinessDay(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .collectList()
                .filter(welfareFundTransaction -> welfareFundTransaction
                        .stream()
                        .map(WelfareFund::getStatus)
                        .allMatch(string -> string.equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Welfare Fund Authorization is Not Completed for Office")))
                .map(welfareFunds -> managementProcessTracker);

    }

    private Mono<ManagementProcessTracker> validateSamityListForAutoVoucherGeneration(ManagementProcessTracker managementProcessTracker) {
        log.info("Validating Samity List for Auto Voucher Generation for Office: {}", managementProcessTracker.getOfficeId());
        return stagingDataUseCase.getStagingProcessEntityByOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .flatMap(this::validateSamityForAutoVoucherGeneration)
                .collectList()
                .map(stagingProcessTracker -> managementProcessTracker)
                .doOnNext(response -> log.info("Samity List for Auto Voucher Generation is Valid for Office: {}", managementProcessTracker.getOfficeId()));
    }

    private Mono<StagingProcessTrackerEntity> validateSamityForAutoVoucherGeneration(StagingProcessTrackerEntity stagingProcessTracker) {
        log.info("Validating Samity {} for Auto Voucher Generation for Samity", stagingProcessTracker.getSamityId());
        return samityEventTrackerUseCase.getAllSamityEventsForSamity(stagingProcessTracker.getManagementProcessId(), stagingProcessTracker.getSamityId())
                .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                .map(SamityEventTracker::getSamityEvent)
                .collectList()
                .doOnNext(samityEventList -> log.info("Samity: {} Samity Event List: {}", stagingProcessTracker.getSamityId(), samityEventList))
                .filter(samityEventList -> samityEventList.isEmpty() || samityEventList.contains(SamityEvents.CANCELED.getValue()) || samityEventList.contains(SamityEvents.AUTHORIZED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity " + stagingProcessTracker.getSamityId() + " is Not Completed")))
                .map(samityEventList -> stagingProcessTracker)
                .doOnNext(response -> log.info("Samity: {} is Valid for Auto Voucher Generation", stagingProcessTracker.getSamityId()));
    }

    private Mono<ManagementProcessTracker> checkIfAutoVoucherGenerationProcessIsAlreadyRunningForOffice(ManagementProcessTracker managementProcessTracker) {
        log.info("Checking if Auto Voucher Generation Process is Already Running for Office: {}", managementProcessTracker.getOfficeId());
        return port.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(dayEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(dayEndProcessTracker.getTransactionCode()))
                .collectList()
                .filter(List::isEmpty)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Auto Voucher Generation Process is Already Running for Office")))
                .map(dayEndProcessTrackerList -> managementProcessTracker)
                .doOnNext(response -> log.info("Auto Voucher Generation Process is Not Running for Office: {}", managementProcessTracker.getOfficeId()));
    }

    private Mono<ManagementProcessTracker> validateOfficeEventListForAutoVoucherGeneration(ManagementProcessTracker managementProcessTracker) {
        log.info("Validating Office Event List for Auto Voucher Generation for Office: {}", managementProcessTracker.getOfficeId());
        return officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .map(OfficeEventTracker::getOfficeEvent)
                .collectList()
                .doOnNext(officeEventList -> log.info("Office Event List for Auto Voucher Generation for Office: {}", officeEventList))
                .filter(officeEventList -> !officeEventList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No office Event Found for Office")))
                .filter(officeEventList -> officeEventList.contains(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data is Not Generated For Office")))
                .filter(officeEventList -> !officeEventList.contains(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed for Office")))
                .filter(officeEventList -> !officeEventList.contains(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Auto Voucher Generation Process is Already Completed for Office")))
                .map(officeEventList -> managementProcessTracker)
                .doOnNext(response -> log.info("Office Event List for Auto Voucher Generation is Valid for Office: {}", managementProcessTracker.getOfficeId()));
    }

    private Mono<Void> generateAutoVoucherForOfficeDayEndProcess(ManagementProcessTracker managementProcessTracker, String loginId) {
        final String AutoVoucherGenerationProcessId = UUID.randomUUID().toString();
        return this.getAndBuildDayEndProcessDetailViewResponseFromTransaction(managementProcessTracker)
                .flatMap(dayEndProcessDetailViewResponseDTO -> {
                    log.info("dayEndProcessDetailViewResponseDTO : {}", dayEndProcessDetailViewResponseDTO);
                    return this.createAndSaveDayEndProcessTrackerEntry(managementProcessTracker.getManagementProcessId(), dayEndProcessDetailViewResponseDTO, loginId, AutoVoucherGenerationProcessId);
                })
                .flatMap(dayEndProcessTrackers -> this.getAccountingAndCreateAutoVoucherEntryForOfficeAndUpdateOfficeEventTracker(managementProcessTracker, dayEndProcessTrackers, loginId))
//                .subscribeOn(Schedulers.immediate())
                .doOnSuccess(dayEndProcessTrackerList -> log.info("Auto Voucher Generation Process is Completed for Office: {}", managementProcessTracker.getOfficeId()))
                .doOnError(throwable -> log.error("Error in Auto Voucher Generation Process: {}", throwable.getMessage()))
                .then();
    }

    private Mono<List<DayEndProcessTracker>> getAccountingAndCreateAutoVoucherEntryForOfficeAndUpdateOfficeEventTracker(ManagementProcessTracker managementProcessTracker, List<DayEndProcessTracker> dayEndProcessTrackerList, String loginId) {
        return this.getAccountingAndUpdateDayEndProcessTrackerEntryV2(dayEndProcessTrackerList)
                .doOnError(throwable -> log.error("Error in Accounting and Updating Day End Process Tracker Entry: {}", throwable.getMessage()))
                .flatMap(processTrackerList -> this.createAutoVoucherEntryForOffice(managementProcessTracker, processTrackerList, loginId))
                .doOnError(throwable -> log.error("Error in Creating Auto Voucher Entry for Office: {}", throwable.getMessage()))
                .flatMap(tuples -> {
                    List<DayEndProcessTracker> trackerList = tuples.getT1();
                    Boolean voucherEntryCreationStatus = tuples.getT2();
                    if (voucherEntryCreationStatus && trackerList.stream().allMatch(dayEndProcessTracker -> dayEndProcessTracker.getStatus().equals(Status.STATUS_FINISHED.getValue()))) {
                        return officeEventTrackerUseCase.insertOfficeEvent(trackerList.get(0).getManagementProcessId(), trackerList.get(0).getOfficeId(), OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue(), loginId, trackerList.get(0).getDayEndProcessTrackerId())
                                .map(officeEventTracker -> trackerList);
                    }
                    return Mono.just(trackerList);
                });
    }

    private Mono<List<DayEndProcessTracker>> getAccountingAndUpdateDayEndProcessTrackerEntryV2(List<DayEndProcessTracker> dayEndProcessTrackerList) {
        return Flux.fromIterable(dayEndProcessTrackerList)
                .flatMap(dayEndProcessTracker -> {
                    if (dayEndProcessTracker.getStatus().equals(Status.STATUS_FINISHED.getValue())) {
                        return Mono.just(dayEndProcessTracker);
                    }
                    return this.runDayEndProcessAccounting(dayEndProcessTracker);
                })
                .collectList();
    }

    private Mono<DayEndProcessTracker> runDayEndProcessAccounting(DayEndProcessTracker dayEndProcessTracker) {
        return port.updateDayEndProcessTrackerEntryStatus(dayEndProcessTracker, Status.STATUS_PROCESSING.getValue())
                .flatMap(this::getAisRequestAndUpdateDayEndProcessTracker)
                .flatMap(processTracker -> {
                    if (!processTracker.getStatus().equalsIgnoreCase(Status.STATUS_FAILED.getValue())) {
                        return port.updateDayEndProcessTrackerEntryStatus(processTracker, Status.STATUS_FINISHED.getValue());
                    }
                    return Mono.just(processTracker);
                });
    }

    private Mono<Tuple2<List<DayEndProcessTracker>, Boolean>> createAutoVoucherEntryForOffice(ManagementProcessTracker managementProcessTracker, List<DayEndProcessTracker> dayEndProcessTrackerList, String loginId) {
        BigDecimal totalVoucherAmount = dayEndProcessTrackerList
                .stream()
                .map(DayEndProcessTracker::getAisRequest)
                .map(aisRequest -> gson.fromJson(aisRequest, JournalRequestDTO.class))
                .map(JournalRequestDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        log.info("Total Voucher Amount for Office: {}", totalVoucherAmount);

        return totalVoucherAmount.compareTo(BigDecimal.ZERO) > 0
                ? autoVoucherUseCase.createAndSaveAutoVoucherFromAISRequest(this.buildAutoVoucherCreationRequestDTO(managementProcessTracker, dayEndProcessTrackerList, loginId))
                .filter(autoVoucherList -> !autoVoucherList.isEmpty())
                .map(autoVoucherList -> Tuples.of(dayEndProcessTrackerList, true))
                : Mono.just(Tuples.of(dayEndProcessTrackerList, true));
    }

    private AutoVoucherRequestDTO buildAutoVoucherCreationRequestDTO(ManagementProcessTracker managementProcessTracker, List<DayEndProcessTracker> dayEndProcessTrackerList, String loginId) {
        List<JournalRequestDTO> aisRequestList = dayEndProcessTrackerList.stream()
                .map(DayEndProcessTracker::getAisRequest)
                .map(aisRequest -> gson.fromJson(aisRequest, JournalRequestDTO.class))
                .filter(journalRequestDTO -> !journalRequestDTO.getJournalType().equalsIgnoreCase(AisMetaDataEnum.PROCESS_NAME_FEE_COLLECTION.getValue()))
                .toList();
        log.info("Auto Voucher Creation AIS Request List for Office: {}", aisRequestList);
        return AutoVoucherRequestDTO.builder()
                .managementProcessId(managementProcessTracker.getManagementProcessId())
                .processId(dayEndProcessTrackerList.get(0).getDayEndProcessTrackerId())
                .officeId(managementProcessTracker.getOfficeId())
                .mfiId(managementProcessTracker.getMfiId())
                .businessDate(managementProcessTracker.getBusinessDate())
                .loginId(loginId)
                .aisRequestList(aisRequestList)
                .build();
    }

    private Mono<List<DayEndProcessTracker>> validateIfRetryAllForDayEndProcessRequestIsValid(ManagementProcessTracker managementProcessTracker, DayEndProcessRequestDTO requestDTO) {
        return port.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                .filter(dayEndProcessTracker -> dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()))
                .collectList()
                .flatMap(dayEndProcessTrackerList -> {
                    if (dayEndProcessTrackerList.isEmpty()) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process Retry request is Invalid"));
                    }
                    return Mono.just(dayEndProcessTrackerList);
                })
                .flatMap(dayEndProcessTrackerList -> port.updateDayEndProcessEntryListForRetry(dayEndProcessTrackerList, requestDTO.getLoginId()))
                .doOnNext(dayEndProcessTrackerList -> log.info("Day End Process List to Retry: {}", dayEndProcessTrackerList));
    }

    private Mono<List<DayEndProcessTracker>> validateIfDayEndProcessRetryRequestIsValid(ManagementProcessTracker managementProcessTracker, DayEndProcessRequestDTO requestDTO) {
        return port.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId())
                .filter(dayEndProcessTracker -> requestDTO.getTransactionCodeList().stream().anyMatch(transactionCode -> transactionCode.equals(dayEndProcessTracker.getTransactionCode())))
                .filter(dayEndProcessTracker -> dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()))
                .collectList()
                .flatMap(dayEndProcessTrackerList -> {
                    if (dayEndProcessTrackerList.isEmpty() || dayEndProcessTrackerList.size() != requestDTO.getTransactionCodeList().size()) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process Retry request is Invalid"));
                    }
                    return Mono.just(dayEndProcessTrackerList);
                })
                .flatMap(dayEndProcessTrackerList -> port.updateDayEndProcessEntryListForRetry(dayEndProcessTrackerList, requestDTO.getLoginId()))
                .doOnNext(dayEndProcessTrackerList -> log.info("Day End Process List to Retry: {}", dayEndProcessTrackerList));
    }

    private Mono<DayEndProcessDetailViewResponseDTO> buildAndRunDayEndProcessForOffice(ManagementProcessTracker managementProcessTracker, String loginId) {
        final String dayEndProcessTrackerId = UUID.randomUUID().toString();
        return this.getAndBuildDayEndProcessDetailViewResponseFromTransaction(managementProcessTracker)
                .doOnNext(dayEndProcessDetailViewResponseDTO -> this.createAndSaveDayEndProcessTrackerEntry(managementProcessTracker.getManagementProcessId(), dayEndProcessDetailViewResponseDTO, loginId, dayEndProcessTrackerId)
                        .flatMap(dayEndProcessTrackerList -> this.getAccountingAndUpdateDayEndProcessTrackerEntry(managementProcessTracker, dayEndProcessTrackerList, loginId, dayEndProcessTrackerId))
                        .subscribeOn(Schedulers.immediate())
                        .doOnError(throwable -> log.error("Error in Run Day End Process: {}", throwable.getMessage()))
                        .subscribe());
    }

    private Mono<List<DayEndProcessTracker>> getAccountingAndUpdateDayEndProcessTrackerEntry(ManagementProcessTracker managementProcessTracker, List<DayEndProcessTracker> dayEndProcessTrackerList, String loginId, String dayEndProcessTrackerId) {
        return Flux.fromIterable(dayEndProcessTrackerList)
//                .delayElements(Duration.ofSeconds(2))
                .flatMap(dayEndProcessTracker -> port.updateDayEndProcessTrackerEntryStatus(dayEndProcessTracker, Status.STATUS_PROCESSING.getValue()))
                .flatMap(this::getAccountingForDayEndProcessTracker)
                .collectList()
                .flatMap(trackerList -> {
                    if (trackerList.stream().allMatch(dayEndProcessTracker -> dayEndProcessTracker.getStatus().equals(Status.STATUS_FINISHED.getValue()))) {
                        return officeEventTrackerUseCase.insertOfficeEvent(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue(), loginId, dayEndProcessTrackerId)
                                .map(officeEventTracker -> trackerList);
                    }
                    return Mono.just(trackerList);
                });
    }

    private Mono<DayEndProcessTracker> getAccountingForDayEndProcessTracker(DayEndProcessTracker dayEndProcessTracker) {
        return this.getAisRequestAndUpdateDayEndProcessTracker(dayEndProcessTracker)
//                .delayElement(Duration.ofSeconds(2))
                .flatMap(this::getAisResponseAndUpdateDayEndProcessTracker);
//                .delayElement(Duration.ofSeconds(2))
//                .map(this::checkIfDayEndProcessAccountingIsSucceededOrFailed);
    }

    private Boolean checkIfDayEndProcessAccountingIsSucceededOrFailed(DayEndProcessTracker dayEndProcessTracker) {
//        @TODO: check if accounting is failed or succeeded
        return !dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue());
    }

    private Mono<DayEndProcessTracker> getAisRequestAndUpdateDayEndProcessTracker(DayEndProcessTracker dayEndProcessTracker) {
        String processId = dayEndProcessTracker.getDayEndProcessTrackerId().concat("_").concat(dayEndProcessTracker.getTransactionCode());
        return metaPropertyUseCase.getAccountingMetaProperty()
                .doOnNext(accountingMetaProperty -> log.info("Accounting Meta Property for getAisRequestAndUpdateDayEndProcessTracker : {}", accountingMetaProperty))
                .flatMap(accountingMetaProperty -> this.buildAccountingRequestDTO(dayEndProcessTracker, accountingMetaProperty))
                .flatMap(accountingUseCase::getAccountingJournalRequestBody)
                .doOnError(throwable -> log.error("Error in getting AIS Request: {}", throwable.getMessage()))
                .onErrorReturn(JournalRequestDTO.builder()
                        .description("Error in getting AIS Request")
                        .build())
                .flatMap(aisRequest -> {

                    /*Tuple2<JournalRequestDTO, Boolean> dtoBooleanTuple2 = validateDebitAndCreditAmount(aisRequest);
                    if (!dtoBooleanTuple2.getT2()) {
                        dayEndProcessTracker.setRemarks("Debit and Credit Amount are not Equal.");
                        return port.updateDayEndProcessTrackerEntryStatus(dayEndProcessTracker, Status.STATUS_FAILED.getValue());
                    }*/

                    if (aisRequest.getDescription().equals("Error in getting AIS Request")) {
                        return port.updateDayEndProcessTrackerEntryAisRequest(dayEndProcessTracker, aisRequest)
                                .flatMap(dayEndProcessTracker1 -> port.updateDayEndProcessTrackerEntryStatus(dayEndProcessTracker, Status.STATUS_FAILED.getValue()));
                    }
                    aisRequest.setProcessId(processId);
                    return port.updateDayEndProcessTrackerEntryAisRequest(dayEndProcessTracker, aisRequest);
                });
    }


    private Tuple2<JournalRequestDTO, Boolean> validateDebitAndCreditAmount(JournalRequestDTO requestDTO) {
        List<Journal> journalRequestDTOList = requestDTO.getJournalList();
        BigDecimal debitAmount = journalRequestDTOList.stream()
                .map(Journal::getDebitedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal creditAmount = journalRequestDTOList.stream()
                .map(Journal::getCreditedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (debitAmount.compareTo(creditAmount) != 0) {
            return Tuples.of(requestDTO, false);
        }
        return Tuples.of(requestDTO, true);
    }

    private Mono<DayEndProcessTracker> getAisResponseAndUpdateDayEndProcessTracker(DayEndProcessTracker dayEndProcessTracker) {
//        @TODO: get AIS Response For AisRequest
        if (dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue())) {
            return Mono.just(dayEndProcessTracker);
        } else {
            return Mono.just(this.getJournalRequestDTOForDayEndProcessTracker(dayEndProcessTracker))
                    .doOnNext(journalRequestDTO -> log.info("Journal Request DTO: {}", journalRequestDTO))
                    .doOnError(throwable -> log.error("Error in getting AIS Response: {}", throwable.getMessage()))
                    .flatMap(journalRequestDTO -> {
                        if (journalRequestDTO.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                            return accountingUseCase.saveAccountingJournal(journalRequestDTO);
                        }
                        return Mono.just(AisResponse.builder()
                                .message("Accounting is Not Called due to Zero Amount in Journal Request DTO")
                                .build());
                    })
                    .doOnNext(aisResponse -> log.info("Ais Response: {}", aisResponse))
                    .doOnError(throwable -> log.error("Error in Ais Response: {}", throwable.getMessage()))
                    .onErrorReturn(AisResponse.builder()
                            .message("Error in getting AIS Response")
                            .build())
                    .flatMap(aisResponse -> {
                        if (aisResponse.getMessage().equals("Error in getting AIS Response") && !dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue())) {
                            return port.updateDayEndProcessTrackerEntryAisResponse(dayEndProcessTracker, aisResponse.toString())
                                    .flatMap(dayEndProcessTracker1 -> port.updateDayEndProcessTrackerEntryStatus(dayEndProcessTracker, Status.STATUS_FAILED.getValue()));
                        }
                        return port.updateDayEndProcessTrackerEntryAisResponse(dayEndProcessTracker, aisResponse.toString())
                                .flatMap(dayEndProcessTracker1 -> port.updateDayEndProcessTrackerEntryStatus(dayEndProcessTracker, Status.STATUS_FINISHED.getValue()));
                    });
        }
    }

    private JournalRequestDTO getJournalRequestDTOForDayEndProcessTracker(DayEndProcessTracker dayEndProcessTracker) {
        return gson.fromJson(dayEndProcessTracker.getAisRequest(), JournalRequestDTO.class);
    }

    private Mono<AccountingRequestDTO> buildAccountingRequestDTO(DayEndProcessTracker dayEndProcessTracker, AccountingMetaProperty accountingMetaProperty) {
        AtomicReference<String> processName = new AtomicReference<>();
        if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.LOAN_DISBURSEMENT.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_DISBURSEMENT.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.LOAN_REPAY.getValue())) {
            processName.set(accountingMetaProperty.getAllowAdvanceJournal()
                    ? AisMetaDataEnum.PROCESS_NAME_LOAN_COLLECTION.getValue()
                    : AisMetaDataEnum.PROCESS_NAME_LOAN_COLLECTION_NO_ADVANCE.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.SAVINGS_DEPOSIT.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_SAVINGS_COLLECTION.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.SAVINGS_WITHDRAW.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_WITHDRAW.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.LOAN_ADJUSTMENT.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_LOAN_ADJUSTMENT.getValue());
        } /*else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.SC_PROVISIONING.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_SC_PROVISION.getValue());
        }*/ else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.WELFARE_FUND.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_WELFARE_FUND.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.FEE_COLLECTION.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_FEE_COLLECTION.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.INTEREST_DEPOSIT.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_INTEREST_POSTING.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.REVERSE_LOAN_REPAY.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_REVERSE_LOAN_REPAY.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.REVERSE_SAVINGS_DEPOSIT.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_REVERSE_SAVINGS_DEPOSIT.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.REVERSE_SAVINGS_WITHDRAW.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_REVERSE_SAVINGS_WITHDRAW.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.ADJUSTMENT_SAVINGS_DEPOSIT.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_SAVINGS_DEPOSIT.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.ADJUSTMENT_SAVINGS_WITHDRAW.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_SAVINGS_WITHDRAW.getValue());
        } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.ADJUSTMENT_LOAN_REPAY.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_ADJUSTMENT_LOAN_REPAY.getValue());
        }
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(dayEndProcessTracker.getOfficeId())
                .map(managementProcessTracker -> AccountingRequestDTO.builder()
                        .managementProcessId(dayEndProcessTracker.getManagementProcessId())
                        .mfiId(managementProcessTracker.getMfiId())
                        .loginId(dayEndProcessTracker.getCreatedBy())
                        .officeId(dayEndProcessTracker.getOfficeId())
                        .processName(processName.get())
                        .accountingMetaProperty(accountingMetaProperty)
                        .build());
    }


    private Mono<ManagementProcessTracker> validateIfDayEndProcessIsRunnable(DayEndProcessRequestDTO requestDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .flatMap(managementProcessTracker -> officeEventTrackerUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .collectList()
                        .filter(officeEventTrackerList -> officeEventTrackerList.stream().anyMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Staging Data Generation is Not Completed For Office")))
                        .filter(officeEventTrackerList -> officeEventTrackerList.stream().noneMatch(officeEventTracker -> officeEventTracker.getOfficeEvent().equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Completed For Office")))
                        .map(officeEventTrackerList -> managementProcessTracker))
                .flatMap(managementProcessTracker -> port.getDayEndProcessTrackerEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .collectList()
                        .doOnNext(dayEndProcessTrackerList -> log.debug("Day End Process Tracker List: {}", dayEndProcessTrackerList))
                        .flatMap(dayEndProcessTrackerList -> {
                            if (!dayEndProcessTrackerList.isEmpty()) {
                                return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Already Running For Office"));
                            }
                            return Mono.just(managementProcessTracker);
                        }))
                .flatMap(managementProcessTracker -> this.validateIfAllSamityIsAuthorizedForOffice(managementProcessTracker, requestDTO.getOfficeId()))
                .flatMap(this::validateIfWelfareFundTransactionIsAuthorized);
    }

    private Mono<ManagementProcessTracker> validateIfWelfareFundTransactionIsAuthorized(ManagementProcessTracker managementProcessTracker) {
        return welfareFundUseCase.getAllWelfareFundTransactionForOfficeOnABusinessDay(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(welfareFund -> welfareFund.getStatus().equals(Status.STATUS_PENDING.getValue()))
                .collectList()
                .flatMap(welfareFundList -> {
                    if (!welfareFundList.isEmpty()) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Welfare Fund Has Pending Task for Office"));
                    }
                    return Mono.just(managementProcessTracker);
                });
    }

    private Mono<ManagementProcessTracker> validateIfAllSamityIsAuthorizedForOffice(ManagementProcessTracker managementProcessTracker, String officeId) {
        return stagingDataUseCase.getStagingProcessEntityByOffice(managementProcessTracker.getManagementProcessId(), officeId)
                .map(StagingProcessTrackerEntity::getSamityId)
                .flatMap(samityId -> samityEventTrackerUseCase.getAllSamityEventsForSamity(managementProcessTracker.getManagementProcessId(), samityId)
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .filter(samityEventTracker -> !samityEventTracker.getSamityEvent().equals(SamityEvents.CANCELED.getValue()))
                        .collectList()
                        .filter(samityEventTrackerList -> samityEventTrackerList.isEmpty() || samityEventTrackerList.stream().anyMatch(samityEventTracker -> samityEventTracker.getSamityEvent().equals(SamityEvents.AUTHORIZED.getValue())))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with id: " + samityId + " Authorization Process is Pending For Office")))
                )
                .collectList()
                .map(lists -> managementProcessTracker);
    }

    private Mono<List<DayEndProcessTracker>> createAndSaveDayEndProcessTrackerEntry(String managementProcessId, DayEndProcessDetailViewResponseDTO responseDTO, String loginId, String dayEndProcessTrackerId) {
        List<DayEndProcessTracker> dayEndProcessTrackerList = new ArrayList<>();
        dayEndProcessTrackerList.add(this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.LOAN_REPAY.getValue(), responseDTO.getLoanCollection(), Status.STATUS_WAITING.getValue(), loginId));
        dayEndProcessTrackerList.add(this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.SAVINGS_DEPOSIT.getValue(), responseDTO.getSavingsCollection(), Status.STATUS_WAITING.getValue(), loginId));
        dayEndProcessTrackerList.add(this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.SAVINGS_WITHDRAW.getValue(), responseDTO.getSavingsWithdraw(), Status.STATUS_WAITING.getValue(), loginId));
        dayEndProcessTrackerList.add(this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.LOAN_ADJUSTMENT.getValue(), responseDTO.getLoanAdjustment(), Status.STATUS_WAITING.getValue(), loginId));
        dayEndProcessTrackerList.add(this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.LOAN_DISBURSEMENT.getValue(), responseDTO.getLoanDisbursement(), Status.STATUS_WAITING.getValue(), loginId));
//        dayEndProcessTrackerList.add(this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.SC_PROVISIONING.getValue(), responseDTO.getServiceChargeProvisioning(), Status.STATUS_WAITING.getValue(), loginId));
        dayEndProcessTrackerList.add(this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.WELFARE_FUND.getValue(), responseDTO.getWelfareFundCollection(), Status.STATUS_WAITING.getValue(), loginId));
        dayEndProcessTrackerList.add(this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.FEE_COLLECTION.getValue(), responseDTO.getFeeCollection(), Status.STATUS_WAITING.getValue(), loginId));
        dayEndProcessTrackerList.add(this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.INTEREST_DEPOSIT.getValue(), responseDTO.getInterestDeposit(), Status.STATUS_WAITING.getValue(), loginId));

        log.debug("Day End Process tracker List: {}", dayEndProcessTrackerList);

        DayEndProcessTracker reverseLoanRepay = this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.REVERSE_LOAN_REPAY.getValue(), responseDTO.getReverseLoanRepay(), Status.STATUS_WAITING.getValue(), loginId);
        DayEndProcessTracker reverseSavingsDeposit = this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.REVERSE_SAVINGS_DEPOSIT.getValue(), responseDTO.getReverseSavingsDeposit(), Status.STATUS_WAITING.getValue(), loginId);
        DayEndProcessTracker reverseSavingsWithdraw = this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.REVERSE_SAVINGS_WITHDRAW.getValue(), responseDTO.getReverseSavingsWithdraw(), Status.STATUS_WAITING.getValue(), loginId);
        DayEndProcessTracker adjustmentSavingsDeposit = this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.ADJUSTMENT_SAVINGS_DEPOSIT.getValue(), responseDTO.getAdjustmentSavingsDeposit(), Status.STATUS_WAITING.getValue(), loginId);
        DayEndProcessTracker adjustmentSavingsWithdraw = this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.ADJUSTMENT_SAVINGS_WITHDRAW.getValue(), responseDTO.getAdjustmentSavingsWithdraw(), Status.STATUS_WAITING.getValue(), loginId);
        DayEndProcessTracker adjustmentLoanRepay = this.buildDayEndProcessTrackerEntry(managementProcessId, dayEndProcessTrackerId, responseDTO.getOfficeId(), TransactionCodes.ADJUSTMENT_LOAN_REPAY.getValue(), responseDTO.getAdjustmentLoanRepay(), Status.STATUS_WAITING.getValue(), loginId);
        if(reverseLoanRepay.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            dayEndProcessTrackerList.add(reverseLoanRepay);
        }
        if(reverseSavingsDeposit.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            dayEndProcessTrackerList.add(reverseSavingsDeposit);
        }
        if(reverseSavingsWithdraw.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            dayEndProcessTrackerList.add(reverseSavingsWithdraw);
        }
        if(adjustmentSavingsDeposit.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            dayEndProcessTrackerList.add(adjustmentSavingsDeposit);
        }
        if(adjustmentSavingsWithdraw.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            dayEndProcessTrackerList.add(adjustmentSavingsWithdraw);
        }
        if(adjustmentLoanRepay.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            dayEndProcessTrackerList.add(adjustmentLoanRepay);
        }


        List<DayEndProcessTracker> nonZeroAmountlist = dayEndProcessTrackerList
                .stream()
//                .filter(dayEndProcessTracker -> dayEndProcessTracker.getTotalAmount().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        return port.saveDayEndProcessTrackerEntryList(nonZeroAmountlist);
    }

    private DayEndProcessTracker buildDayEndProcessTrackerEntry(String managementProcessId, String dayEndProcessTrackerId, String officeId, String transactionCode, DayEndProcessTransaction dayEndProcessTransaction, String status, String loginId) {
        return DayEndProcessTracker.builder()
                .managementProcessId(managementProcessId)
                .dayEndProcessTrackerId(dayEndProcessTrackerId)
                .officeId(officeId)
                .transactionCode(transactionCode)
                .transactions(dayEndProcessTransaction.getData() != null ? dayEndProcessTransaction.getData() : new ArrayList<>())
                .totalAmount(dayEndProcessTransaction.getTotalAmount())
                .status(status)
                .createdBy(loginId)
                .createdOn(LocalDateTime.now())
                .build();
    }


    private Mono<DayEndProcessDetailViewResponseDTO> getAndBuildDayEndProcessDetailViewResponseFromTransaction(ManagementProcessTracker managementProcessTracker) {
        return transactionUseCase.getAllTransactionsOnABusinessDayForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getBusinessDate())
                .doOnNext(transactionList -> log.info("Office Id: {}, Management Process Id: {}, Business Date: {}, Total Transaction List Size: {}", managementProcessTracker.getOfficeId(), managementProcessTracker.getManagementProcessId(), managementProcessTracker.getBusinessDate(), transactionList.size()))
                .flatMap(transactionList -> this.getTransactionAmountDataForDayEndProcess(managementProcessTracker, transactionList))
//                .flatMap(this::getServiceChargeProvisioningAndBuildDayEndProcessDetailViewResponse)
                .flatMap(this::getWelfareFundAndBuildDayEndProcessDetailViewResponse)
                .flatMap(this::getInterestDepositAndBuildDayEndProcessDetailViewResponse)
                .flatMap(this::getFeeCollectionAndBuildDayEndProcessDetailViewResponse)
                .flatMap(dayEndProcessDetailViewResponseDTO -> this.setStatusAndBtnStatesForDayEndProcessDetailsViewResponse(managementProcessTracker.getManagementProcessId(), dayEndProcessDetailViewResponseDTO))
                .flatMap(this::setBtnStatesForWelfareFundForDayEndProcessDetailViewResponse);
    }

    private Mono<DayEndProcessDetailViewResponseDTO> getInterestDepositAndBuildDayEndProcessDetailViewResponse(DayEndProcessDetailViewResponseDTO responseDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(responseDTO.getOfficeId())
                .flatMap(managementProcessTracker -> passbookUseCase.getPassbookEntriesByTransactionCodeAndManagementProcessId(TransactionCodes.INTEREST_DEPOSIT.getValue(), managementProcessTracker.getManagementProcessId()))
                .flatMap(interestDepositTransactionList -> {
                    if (interestDepositTransactionList.isEmpty()) {
                        DayEndProcessTransaction interestDeposit = DayEndProcessTransaction.builder()
                                .data(new ArrayList<>())
                                .totalAmount(BigDecimal.ZERO)
                                .status(Status.STATUS_WAITING.getValue())
                                .btnRetryEnabled("No")
                                .build();
                        responseDTO.setInterestDeposit(interestDeposit);
                        return Mono.just(responseDTO);
                    }
                    return this.buildDayEndProcessDetailViewResponseForInterestDeposit(responseDTO, interestDepositTransactionList);
                });
    }

    private Mono<DayEndProcessDetailViewResponseDTO> buildDayEndProcessDetailViewResponseForInterestDeposit(DayEndProcessDetailViewResponseDTO responseDTO, List<PassbookResponseDTO> interestDepositTransactionList) {
        List<String> savingsAccountIdList = interestDepositTransactionList.stream().map(PassbookResponseDTO::getSavingsAccountId).filter(Objects::nonNull).toList();
        return commonRepository.getSavingsProductDetailsBySavingsAccountList(savingsAccountIdList)
                .collectList()
                .map(accountWithProductEntityList -> this.getProductAccountIdListMap(TransactionCodes.INTEREST_DEPOSIT.getValue(), accountWithProductEntityList))
                .map(productAccountIdListMap -> {
                    List<DayEndProcessProductTransaction> data = new ArrayList<>();
                    productAccountIdListMap.forEach((productId, productWithAccountId) -> {
                        BigDecimal totalAmount = interestDepositTransactionList.stream()
                                .filter(passbookResponseDTO -> productWithAccountId.getAccountIdList().contains(passbookResponseDTO.getSavingsAccountId()))
                                .map(PassbookResponseDTO::getDepositAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        data.add(DayEndProcessProductTransaction.builder()
                                .productId(productId)
                                .productNameEn(productWithAccountId.getProductNameEn())
                                .productNameBn(productWithAccountId.getProductNameBn())
                                .amount(totalAmount)
                                .build());
                    });
                    responseDTO.setInterestDeposit(DayEndProcessTransaction.builder()
                            .transactionCode(TransactionCodes.INTEREST_DEPOSIT.getValue())
                            .data(data)
                            .totalAmount(data.stream().map(DayEndProcessProductTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                            .build());
                    return responseDTO;
                });
    }

    private Mono<DayEndProcessDetailViewResponseDTO> getFeeCollectionAndBuildDayEndProcessDetailViewResponse(DayEndProcessDetailViewResponseDTO dayEndProcessDetailViewResponseDTO) {
        return feeCollectionUseCase.getFeeCollectionByOfficeIdForCurrentDay(dayEndProcessDetailViewResponseDTO.getOfficeId())
                .collectList()
                .flatMap(feeCollectionList -> {
                    if (feeCollectionList.isEmpty()) {
                        DayEndProcessTransaction feeCollection = DayEndProcessTransaction.builder()
                                .data(new ArrayList<>())
                                .totalAmount(BigDecimal.ZERO)
                                .status(Status.STATUS_WAITING.getValue())
                                .btnRetryEnabled("No")
                                .build();
                        dayEndProcessDetailViewResponseDTO.setFeeCollection(feeCollection);
                        return Mono.just(dayEndProcessDetailViewResponseDTO);
                    }
                    return this.buildDayEndProcessDetailViewResponseForFeeCollection(dayEndProcessDetailViewResponseDTO, feeCollectionList,dayEndProcessDetailViewResponseDTO.getOfficeId());
                });
    }

    private Mono<DayEndProcessDetailViewResponseDTO> buildDayEndProcessDetailViewResponseForFeeCollection(DayEndProcessDetailViewResponseDTO dayEndProcessDetailViewResponseDTO, List<FeeCollection> feeCollectionList, String officeId) {
        Map<String, BigDecimal> feeCollectionSumByFeeSetting = feeCollectionList.stream()
                .collect(Collectors.groupingBy(
                        FeeCollection::getFeeTypeSettingId,
                        Collectors.mapping(FeeCollection::getAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));

        return Flux.fromIterable(feeCollectionSumByFeeSetting.entrySet())
                .flatMap(entry -> feeTypeSettingUseCase.getFeeTypeSettingBySettingId(entry.getKey())
                        .map(setting -> DayEndProcessProductTransaction.builder()
                                .productId(setting.getFeeTypeSettingId())
                                .productNameBn(setting.getFeeTypeNameBn())
                                .productNameEn(setting.getFeeTypeNameEn())
                                .amount(entry.getValue())
                                .build())
                )
                .collectList()
                .flatMap(feeCollectionTransactions -> {
                    dayEndProcessDetailViewResponseDTO.setFeeCollection(DayEndProcessTransaction.builder()
                            .transactionCode(TransactionCodes.FEE_COLLECTION.getValue())
                            .data(feeCollectionTransactions)
                            .totalAmount(feeCollectionList.stream().map(FeeCollection::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                            .build());
                    return Mono.just(dayEndProcessDetailViewResponseDTO);
                });
    }

    private Mono<DayEndProcessDetailViewResponseDTO> setBtnStatesForWelfareFundForDayEndProcessDetailViewResponse(DayEndProcessDetailViewResponseDTO responseDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(responseDTO.getOfficeId())
                .flatMap(managementProcessTracker -> welfareFundUseCase.getAllWelfareFundTransactionForOfficeOnABusinessDay(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .filter(welfareFund -> welfareFund.getStatus().equals(Status.STATUS_PENDING.getValue()))
                        .collectList())
                .map(welfareFunds -> {
                    if (!welfareFunds.isEmpty()) {
                        if (welfareFunds.stream().anyMatch(welfareFundTransaction -> welfareFundTransaction.getStatus().equals(Status.STATUS_PENDING.getValue()))) {
                            responseDTO.setBtnRunDayEndProcessEnabled("No");
                            responseDTO.setBtnRefreshEnabled("No");
                            responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                            responseDTO.setUserMessage("Day End Process is Not Completed For Office");
                        }
                    }
                    return responseDTO;
                });
    }

    private Mono<DayEndProcessDetailViewResponseDTO> getWelfareFundAndBuildDayEndProcessDetailViewResponse(DayEndProcessDetailViewResponseDTO responseDTO) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(responseDTO.getOfficeId())
                .flatMap(managementProcessTracker -> transactionUseCase.getWelfareFundTransactionForOfficeByManagementProcessId(managementProcessTracker.getManagementProcessId()))
                .flatMap(welfareFundTransactionList -> {
                    if (welfareFundTransactionList.isEmpty()) {
                        DayEndProcessTransaction welfareFund = DayEndProcessTransaction.builder()
                                .data(new ArrayList<>())
                                .totalAmount(BigDecimal.ZERO)
                                .status(Status.STATUS_WAITING.getValue())
                                .btnRetryEnabled("No")
                                .build();
                        responseDTO.setWelfareFundCollection(welfareFund);
                        return Mono.just(responseDTO);
                    }
                    return this.BuildDayEndProcessDetailViewResponseForWelfareFund(responseDTO, welfareFundTransactionList);
                });
    }

    private Mono<DayEndProcessDetailViewResponseDTO> BuildDayEndProcessDetailViewResponseForWelfareFund(DayEndProcessDetailViewResponseDTO responseDTO, List<Transaction> welfareFundTransactionList) {
        List<String> loanAccountIdList = welfareFundTransactionList.stream().map(Transaction::getLoanAccountId).filter(Objects::nonNull).toList();
        return commonRepository.getLoanProductDetailsByLoanAccountList(loanAccountIdList)
                .collectList()
                .map(accountWithProductEntityList -> this.getProductAccountIdListMap(TransactionCodes.WELFARE_FUND.getValue(), accountWithProductEntityList))
                .map(productAccountIdListMap -> {
                    List<DayEndProcessProductTransaction> data = new ArrayList<>();
                    productAccountIdListMap.forEach((productId, productWithAccountId) -> {
                        BigDecimal totalAmount = welfareFundTransactionList.stream()
                                .filter(transaction -> productWithAccountId.getAccountIdList().contains(transaction.getLoanAccountId()))
                                .map(Transaction::getAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        data.add(DayEndProcessProductTransaction.builder()
                                .productId(productId)
                                .productNameEn(productWithAccountId.getProductNameEn())
                                .productNameBn(productWithAccountId.getProductNameBn())
                                .amount(totalAmount)
                                .build());
                    });
                    responseDTO.setWelfareFundCollection(DayEndProcessTransaction.builder()
                            .transactionCode(TransactionCodes.WELFARE_FUND.getValue())
                            .data(data)
                            .totalAmount(data.stream().map(DayEndProcessProductTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                            .build());
                    return responseDTO;
                });
    }


    private Mono<DayEndProcessDetailViewResponseDTO> getServiceChargeProvisioningAndBuildDayEndProcessDetailViewResponse(DayEndProcessDetailViewResponseDTO responseDTO) {
        return metaPropertyUseCase.getAccountingMetaProperty()
                .doOnNext(accountingMetaProperty -> log.info("Accounting Meta Property: {}", accountingMetaProperty))
                .doOnError(throwable -> log.error("Error in getting Accounting Meta Property: {}", throwable.getMessage()))
                .flatMap(accountingMetaProperty -> {
                    if (accountingMetaProperty.getAllowSCProvision()) {
                        log.info("Service Charge Provisioning is Allowed");
                        return calendarUseCase.getNextBusinessDateForOffice(responseDTO.getOfficeId(), responseDTO.getBusinessDate())
                                .flatMap(nextBusinessDate -> /*loanRepaymentScheduleUseCase.getRepaymentScheduleByInstallmentDate(nextBusinessDate)*/
                                        loanRepaymentScheduleUseCase.getUnprovisionedRepaymentSchedulesByInstallmentDate(nextBusinessDate, responseDTO.getOfficeId())
                                                .collectList())
                                .flatMap(repaymentScheduleList -> {
                                    if (repaymentScheduleList.isEmpty()) {
                                        log.info("No Repayment Schedule Found For Service Charge Provisioning");
                                        DayEndProcessTransaction serviceChargeProvisioning = DayEndProcessTransaction.builder()
                                                .data(new ArrayList<>())
                                                .totalAmount(BigDecimal.ZERO)
                                                .status(Status.STATUS_WAITING.getValue())
                                                .btnRetryEnabled("No")
                                                .build();
                                        responseDTO.setServiceChargeProvisioning(serviceChargeProvisioning);
                                        return Mono.just(responseDTO);
                                    }
                                    return this.BuildDayEndProcessDetailViewResponseForServiceChargeProvisioning(responseDTO, repaymentScheduleList);
                                });
                    } else {
                        DayEndProcessTransaction serviceChargeProvisioning = DayEndProcessTransaction.builder()
                                .data(new ArrayList<>())
                                .totalAmount(BigDecimal.ZERO)
                                .status(Status.STATUS_WAITING.getValue())
                                .btnRetryEnabled("No")
                                .build();
                        responseDTO.setServiceChargeProvisioning(serviceChargeProvisioning);
                        return Mono.just(responseDTO);
                    }
                });
    }

    private Mono<DayEndProcessDetailViewResponseDTO> BuildDayEndProcessDetailViewResponseForServiceChargeProvisioning(DayEndProcessDetailViewResponseDTO responseDTO, List<RepaymentScheduleResponseDTO> repaymentScheduleList) {
        List<String> loanAccountIdList = repaymentScheduleList.stream().map(RepaymentScheduleResponseDTO::getLoanAccountId).toList();
        return commonRepository.getLoanProductDetailsByLoanAccountList(loanAccountIdList)
                .collectList()
                .map(accountWithProductEntityList -> this.getProductAccountIdListMap(TransactionCodes.SC_PROVISIONING.getValue(), accountWithProductEntityList))
                .map(productAccountIdListMap -> {
                    List<DayEndProcessProductTransaction> data = new ArrayList<>();
                    productAccountIdListMap.forEach((productId, productWithAccountId) -> {
                        BigDecimal totalAmount = repaymentScheduleList.stream()
                                .filter(repaymentScheduleResponse -> productWithAccountId.getAccountIdList().contains(repaymentScheduleResponse.getLoanAccountId()))
                                .map(RepaymentScheduleResponseDTO::getServiceCharge)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        data.add(DayEndProcessProductTransaction.builder()
                                .productId(productId)
                                .productNameEn(productWithAccountId.getProductNameEn())
                                .productNameBn(productWithAccountId.getProductNameBn())
                                .amount(totalAmount)
                                .build());
                    });
                    responseDTO.setServiceChargeProvisioning(DayEndProcessTransaction.builder()
                            .transactionCode(TransactionCodes.SC_PROVISIONING.getValue())
                            .data(data)
                            .totalAmount(data.stream().map(DayEndProcessProductTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                            .build());
                    return responseDTO;
                });
    }

    private Mono<DayEndProcessDetailViewResponseDTO> setStatusAndBtnStatesForDayEndProcessDetailsViewResponse(String managementProcessId, DayEndProcessDetailViewResponseDTO responseDTO) {
        return this.getDayEndProcessEntriesAndOfficeEventsTuple(managementProcessId, responseDTO.getOfficeId())
                .map(tuples -> {
                    List<String> officeEvents = tuples.getT1();
                    List<DayEndProcessTracker> dayEndProcessTrackerList = tuples.getT2();
                    if (officeEvents.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))) {
                        responseDTO.setBtnRunDayEndProcessEnabled("No");
                        responseDTO.setBtnRefreshEnabled("Yes");
                        responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                        responseDTO.setUserMessage("Day End Process is Completed For Office");
                        this.setBtnRetryAndStatusForDayEndProcess(responseDTO, dayEndProcessTrackerList);
                    } else if (!dayEndProcessTrackerList.isEmpty()) {
                        this.setBtnRetryAndStatusForDayEndProcess(responseDTO, dayEndProcessTrackerList);
                        responseDTO.setBtnRunDayEndProcessEnabled("No");
                        responseDTO.setBtnRefreshEnabled("Yes");
                        responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                        responseDTO.setUserMessage("Day End Process is Running For Office");
                    } else {
                        responseDTO.getLoanCollection().setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.getSavingsCollection().setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.getSavingsWithdraw().setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.getLoanAdjustment().setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.getLoanDisbursement().setStatus(Status.STATUS_WAITING.getValue());
//                        responseDTO.getAccruedInterest().setStatus(Status.STATUS_WAITING.getValue());
//                        responseDTO.getServiceChargeProvisioning().setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.getWelfareFundCollection().setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.getFeeCollection().setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.getInterestDeposit().setStatus(Status.STATUS_WAITING.getValue());

                        responseDTO.getLoanCollection().setBtnRetryEnabled("No");
                        responseDTO.getSavingsCollection().setBtnRetryEnabled("No");
                        responseDTO.getSavingsWithdraw().setBtnRetryEnabled("No");
                        responseDTO.getLoanAdjustment().setBtnRetryEnabled("No");
                        responseDTO.getLoanDisbursement().setBtnRetryEnabled("No");
//                        responseDTO.getAccruedInterest().setBtnRetryEnabled("No");
//                        responseDTO.getServiceChargeProvisioning().setBtnRetryEnabled("No");
                        responseDTO.getWelfareFundCollection().setBtnRetryEnabled("No");
                        responseDTO.getFeeCollection().setBtnRetryEnabled("No");
                        responseDTO.getInterestDeposit().setBtnRetryEnabled("No");

                        responseDTO.setBtnRunDayEndProcessEnabled("Yes");
                        responseDTO.setBtnRefreshEnabled("No");
                        responseDTO.setBtnRetryEnabled("No");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                        responseDTO.setUserMessage("Day End Process is Not Completed For Office");
                    }
                    return responseDTO;
                })
                .flatMap(dayEndProcessDetailViewResponseDTO -> this.gridViewOfDayEndProcessForOffice(DayEndProcessRequestDTO.builder()
                                .officeId(responseDTO.getOfficeId())
                                .build())
                        .map(dayEndProcessGridViewResponseDTO -> {
                            if (dayEndProcessGridViewResponseDTO.getBtnStartEnabled().equals("No")) {
                                dayEndProcessDetailViewResponseDTO.setBtnRunDayEndProcessEnabled("No");
                                dayEndProcessDetailViewResponseDTO.setBtnRefreshEnabled("Yes");
                            }
                            return dayEndProcessDetailViewResponseDTO;
                        }));
    }

    private void setBtnRetryAndStatusForDayEndProcess(DayEndProcessDetailViewResponseDTO responseDTO, List<DayEndProcessTracker> dayEndProcessTrackerList) {
        dayEndProcessTrackerList.forEach(dayEndProcessTracker -> {
            if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.LOAN_REPAY.getValue())) {
                responseDTO.getLoanCollection().setStatus(dayEndProcessTracker.getStatus());
                responseDTO.getLoanCollection().setBtnRetryEnabled(dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()) ? "Yes" : "No");
            } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.SAVINGS_DEPOSIT.getValue())) {
                responseDTO.getSavingsCollection().setStatus(dayEndProcessTracker.getStatus());
                responseDTO.getSavingsCollection().setBtnRetryEnabled(dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()) ? "Yes" : "No");
            } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.SAVINGS_WITHDRAW.getValue())) {
                responseDTO.getSavingsWithdraw().setStatus(dayEndProcessTracker.getStatus());
                responseDTO.getSavingsWithdraw().setBtnRetryEnabled(dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()) ? "Yes" : "No");
            } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.LOAN_ADJUSTMENT.getValue())) {
                responseDTO.getLoanAdjustment().setStatus(dayEndProcessTracker.getStatus());
                responseDTO.getLoanAdjustment().setBtnRetryEnabled(dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()) ? "Yes" : "No");
            } else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.LOAN_DISBURSEMENT.getValue())) {
                responseDTO.getLoanDisbursement().setStatus(dayEndProcessTracker.getStatus());
                responseDTO.getLoanDisbursement().setBtnRetryEnabled(dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()) ? "Yes" : "No");
            } /*else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.SC_PROVISIONING.getValue())) {
                responseDTO.getServiceChargeProvisioning().setStatus(dayEndProcessTracker.getStatus());
                responseDTO.getServiceChargeProvisioning().setBtnRetryEnabled(dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()) ? "Yes" : "No");
            }*/ else if (dayEndProcessTracker.getTransactionCode().equals(TransactionCodes.WELFARE_FUND.getValue())) {
                responseDTO.getWelfareFundCollection().setStatus(dayEndProcessTracker.getStatus());
                responseDTO.getWelfareFundCollection().setBtnRetryEnabled(dayEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()) ? "Yes" : "No");
            }
        });
    }

    private Mono<DayEndProcessDetailViewResponseDTO> getTransactionAmountDataForDayEndProcess(ManagementProcessTracker managementProcessTracker, List<Transaction> transactionList) {
        return this.getLoanProductListForTransactions(transactionList)
                .flatMap(loanAccountWithProductList -> this.getSavingsProductListForTransactions(transactionList)
                        .map(savingsAccountWithProductList -> Tuples.of(loanAccountWithProductList, savingsAccountWithProductList)))
                .map(tuples -> this.buildDayEndProcessDetailViewResponse(managementProcessTracker, transactionList, tuples));
    }

    private DayEndProcessDetailViewResponseDTO buildDayEndProcessDetailViewResponse(ManagementProcessTracker managementProcessTracker, List<Transaction> transactionList, Tuple2<List<AccountWithProductEntity>, List<AccountWithProductEntity>> tuples) {
        DayEndProcessTransaction loanCollection = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.LOAN_REPAY.getValue(), tuples.getT1());
        DayEndProcessTransaction savingsCollection = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.SAVINGS_DEPOSIT.getValue(), tuples.getT2());
        DayEndProcessTransaction savingsWithdraw = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.SAVINGS_WITHDRAW.getValue(), tuples.getT2());
        DayEndProcessTransaction loanAdjustment = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.LOAN_ADJUSTMENT.getValue(), tuples.getT1());
        DayEndProcessTransaction loanDisbursement = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.LOAN_DISBURSEMENT.getValue(), tuples.getT1());
        DayEndProcessTransaction reverseLoanRepay = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.REVERSE_LOAN_REPAY.getValue(), tuples.getT1());
        DayEndProcessTransaction adjustmentLoanRepay = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.ADJUSTMENT_LOAN_REPAY.getValue(), tuples.getT1());
        DayEndProcessTransaction reverseSavingsDeposit = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.REVERSE_SAVINGS_DEPOSIT.getValue(), tuples.getT2());
        DayEndProcessTransaction adjustmentSavingsDeposit = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.ADJUSTMENT_SAVINGS_DEPOSIT.getValue(), tuples.getT2());
        DayEndProcessTransaction reverseSavingsWithdraw = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.REVERSE_SAVINGS_WITHDRAW.getValue(), tuples.getT2());
        DayEndProcessTransaction adjustmentSavingsWithdraw = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.ADJUSTMENT_SAVINGS_WITHDRAW.getValue(), tuples.getT2());
//        DayEndProcessTransaction accruedInterest = this.buildDayEndProcessTransactionListByTransactionCodeAndProductId(transactionList, TransactionCodes.SAVINGS_ACCRUE.getValue(), tuples.getT2());

        log.info("Loan Collection: {}", loanCollection);
        log.info("Loan Adjustment: {}", loanAdjustment);
        log.info("Savings Collection: {}", savingsCollection);
        log.info("Reverse Loan Repay: {}", reverseLoanRepay);
        log.info("Adjustment Loan Repay: {}", adjustmentLoanRepay);
        log.info("Reverse Savings Deposit: {}", reverseSavingsDeposit);
        log.info("Adjustment Savings Deposit: {}", adjustmentSavingsDeposit);
        log.info("Reverse Savings Withdraw: {}", reverseSavingsWithdraw);
        log.info("Adjustment Savings Withdraw: {}", adjustmentSavingsWithdraw);


        return DayEndProcessDetailViewResponseDTO.builder()
                .officeId(managementProcessTracker.getOfficeId())
                .officeNameEn(managementProcessTracker.getOfficeNameEn())
                .officeNameBn(managementProcessTracker.getOfficeNameBn())
                .businessDate(managementProcessTracker.getBusinessDate())
                .businessDay(managementProcessTracker.getBusinessDay())
                .loanCollection(loanCollection)
                .savingsCollection(savingsCollection)
                .savingsWithdraw(savingsWithdraw)
                .loanAdjustment(loanAdjustment)
                .loanDisbursement(loanDisbursement)
                .reverseLoanRepay(reverseLoanRepay)
                .adjustmentLoanRepay(adjustmentLoanRepay)
                .reverseSavingsDeposit(reverseSavingsDeposit)
                .adjustmentSavingsDeposit(adjustmentSavingsDeposit)
                .reverseSavingsWithdraw(reverseSavingsWithdraw)
                .adjustmentSavingsWithdraw(adjustmentSavingsWithdraw)
//                .accruedInterest(accruedInterest)
                .build();
    }

    private Mono<List<AccountWithProductEntity>> getSavingsProductListForTransactions(List<Transaction> transactionList) {
        List<String> savingsAccountIdList = transactionList.stream()
                .map(Transaction::getSavingsAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        log.info("Savings Account Id List: {}", savingsAccountIdList);

        if (!savingsAccountIdList.isEmpty()) {
            return commonRepository.getSavingsProductDetailsBySavingsAccountList(savingsAccountIdList)
                    .collectList();
        }
        return Mono.just(new ArrayList<>());
    }

    private Mono<List<AccountWithProductEntity>> getLoanProductListForTransactions(List<Transaction> transactionList) {
        List<String> loanAccountIdList = transactionList.stream()
                .map(Transaction::getLoanAccountId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        log.info("Loan Account Id List: {}", loanAccountIdList);

        if (!loanAccountIdList.isEmpty()) {
            return commonRepository.getLoanProductDetailsByLoanAccountList(loanAccountIdList)
                    .collectList();
        }
        return Mono.just(new ArrayList<>());
    }

    private Map<String, ProductWithAccountId> getProductAccountIdListMap(String transactionCode, List<AccountWithProductEntity> accountProductList) {
        Map<String, ProductWithAccountId> productAccountIdListMap = new HashMap<>();
        if (!accountProductList.isEmpty()) {
            accountProductList.forEach(accountWithProductEntity -> {
                if (!productAccountIdListMap.containsKey(accountWithProductEntity.getProductId())) {
                    productAccountIdListMap.put(
                            accountWithProductEntity.getProductId(),
                            ProductWithAccountId.builder()
                                    .productId(accountWithProductEntity.getProductId())
                                    .productNameEn(accountWithProductEntity.getProductNameEn())
                                    .productNameBn(accountWithProductEntity.getProductNameBn())
                                    .amount(BigDecimal.ZERO)
                                    .accountIdList(new ArrayList<>(List.of(accountWithProductEntity.getAccountId())))
                                    .build());
                } else {
                    productAccountIdListMap.get(accountWithProductEntity.getProductId()).getAccountIdList().add(accountWithProductEntity.getAccountId());
                }
            });
        }
        log.info("Transaction Code: {}, Product Id with Account Id Map: {}", transactionCode, productAccountIdListMap);
        return productAccountIdListMap;
    }

    private DayEndProcessTransaction buildDayEndProcessTransactionListByTransactionCodeAndProductId(List<Transaction> transactionList, String transactionCode, List<AccountWithProductEntity> accountProductList) {
        Map<String, ProductWithAccountId> productAccountIdListMap = this.getProductAccountIdListMap(transactionCode, accountProductList);
        boolean transactionCodeLoanRepay = transactionCode.equalsIgnoreCase(TransactionCodes.LOAN_REPAY.getValue());
        log.info("Transaction Code: {}, Loan Repay: {}, Product Id with Account Id Map: {}", transactionCode, transactionCodeLoanRepay, productAccountIdListMap);
        log.info("Transaction List: {}", transactionList);
        log.info("Transaction List Size: {}", transactionList.size());
        transactionList.stream()
                .filter(transaction -> transactionCodeLoanRepay
                        ? transaction.getTransactionCode().equals(transactionCode) || transaction.getTransactionCode().equals(TransactionCodes.LOAN_REBATE.getValue())
                        : transaction.getTransactionCode().equals(transactionCode))
                .forEach(transaction -> productAccountIdListMap.forEach((productId, productWithAccountId) -> {
                    if (productWithAccountId.getAccountIdList().stream().anyMatch(accountId -> (!HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) && accountId.equals(transaction.getLoanAccountId())) || (!HelperUtil.checkIfNullOrEmpty(transaction.getSavingsAccountId()) && accountId.equals(transaction.getSavingsAccountId())))) {
                        productWithAccountId.setAmount(productWithAccountId.getAmount().add(transaction.getAmount()));
                    }
                }));
        log.info("Transaction List after filter: {}", transactionList);

        List<DayEndProcessProductTransaction> dayEndProcessProductTransactionList = new ArrayList<>();

        productAccountIdListMap.forEach((productId, productWithAccountId) -> {
            if (productWithAccountId.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                dayEndProcessProductTransactionList.add(DayEndProcessProductTransaction.builder()
                        .productId(productWithAccountId.getProductId())
                        .productNameEn(productWithAccountId.getProductNameEn())
                        .productNameBn(productWithAccountId.getProductNameBn())
                        .amount(productWithAccountId.getAmount())
                        .build());
            }
        });
log.info("Day End Process Product Transaction List: {}", dayEndProcessProductTransactionList);
        return DayEndProcessTransaction.builder()
                .data(dayEndProcessProductTransactionList)
                .transactionCode(transactionCode)
                .totalAmount(dayEndProcessProductTransactionList.stream()
                        .map(DayEndProcessProductTransaction::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();
    }


    private Mono<List<DayEndProcessSamityResponse>> getSamityResponseListForDayEndProcessForOffice(ManagementProcessTracker managementProcessTracker, String officeId) {

        return samityEventTrackerUseCase.getAllSamityEventsForOffice(managementProcessTracker.getManagementProcessId(), officeId)
                .map(samityEventTrackerList -> {
                    Map<String, List<String>> samityWithEventList = new HashMap<>();
                    samityEventTrackerList.stream()
                            .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                            .forEach(samityEventTracker -> {
                                if (samityWithEventList.containsKey(samityEventTracker.getSamityId())) {
                                    samityWithEventList.get(samityEventTracker.getSamityId()).add(samityEventTracker.getSamityEvent());
                                } else {
                                    samityWithEventList.put(samityEventTracker.getSamityId(), new ArrayList<>(List.of(samityEventTracker.getSamityEvent())));
                                }
                            });
                    log.info("Samity With Event List: {}", samityWithEventList);
                    return samityWithEventList;
                })
                .flatMap(samityWithEventList -> this.getTransactionAmountsAndBuildSamityResponseList(managementProcessTracker, samityWithEventList));
    }

    private Mono<List<DayEndProcessSamityResponse>> getTransactionAmountsAndBuildSamityResponseList(ManagementProcessTracker managementProcessTracker, Map<String, List<String>> samityWithEventList) {
        final List<String> samityIdList = new ArrayList<>(samityWithEventList.keySet().stream().toList());
        Mono<Tuple6<Map<String, BigDecimal>, Map<String, BigDecimal>, Map<String, BigDecimal>, Map<String, BigDecimal>, Map<String, BigDecimal>, Map<String, BigDecimal>>> samityWithTotalAmountMap = Mono.zip(
                this.getTotalCollectionAmountForSamityResponse(samityIdList),
                this.getTotalWithdrawAmountForSamityResponse(samityIdList),
                this.getTotalLoanAdjustmentAmountForSamityResponse(samityIdList),
                this.getTotalSavingsAdjustmentAmountForSamityResponse(samityIdList),
                this.getTotalLoanDisbursementAmountForSamityResponse(samityIdList),
                this.getTotalFeeCollectionAmountForSamityResponse(samityIdList, managementProcessTracker.getOfficeId())
                        .doOnSuccess(feeCollectionMap -> samityIdList.addAll(feeCollectionMap.keySet().stream().toList())));

        return samityWithTotalAmountMap
                .flatMapMany(samityAmountMap -> stagingDataUseCase.getStagingProcessEntityByOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId()))
                .filter(trackerEntity -> trackerEntity.getSamityDay().equals(managementProcessTracker.getBusinessDay()) || samityIdList.contains(trackerEntity.getSamityId()))
                .doOnRequest(l -> log.info("Samity Id List For Day End Process Grid View TEST: {}", samityIdList))
                .map(trackerEntity -> {
                    DayEndProcessSamityResponse dayEndProcessSamityResponse = gson.fromJson(trackerEntity.toString(), DayEndProcessSamityResponse.class);
                    dayEndProcessSamityResponse.setSamityType(trackerEntity.getSamityDay().equals(managementProcessTracker.getBusinessDay()) ? "Regular" : "Special");
                    dayEndProcessSamityResponse.setCollectionType(trackerEntity.getIsDownloaded().equals("Yes") ? "Offline" : "Online");
                    return dayEndProcessSamityResponse;
                })
                .collectList()
                .doOnNext(list -> log.info("Samity Id List For Day End Process Grid View: {}", list.stream().map(DayEndProcessSamityResponse::getSamityId).sorted().toList()))
                .zipWith(samityWithTotalAmountMap)
                .map(tuples -> {
                    tuples.getT1().forEach(dayEndProcessSamityResponse -> {
                        dayEndProcessSamityResponse.setCollectionAmount(tuples.getT2().getT1().get(dayEndProcessSamityResponse.getSamityId()));
                        dayEndProcessSamityResponse.setWithdrawAmount(tuples.getT2().getT2().get(dayEndProcessSamityResponse.getSamityId()));
                        dayEndProcessSamityResponse.setLoanAdjustmentAmount(tuples.getT2().getT3().get(dayEndProcessSamityResponse.getSamityId()));
                        dayEndProcessSamityResponse.setDisbursementAmount(tuples.getT2().getT5().get(dayEndProcessSamityResponse.getSamityId()));
                        dayEndProcessSamityResponse.setFeeCollectionAmount(tuples.getT2().getT6().get(dayEndProcessSamityResponse.getSamityId()));

                        String samityStatus = null;
                        if (samityWithEventList.containsKey(dayEndProcessSamityResponse.getSamityId())) {
                            if (samityWithEventList.get(dayEndProcessSamityResponse.getSamityId()).contains(SamityEvents.CANCELED.getValue())) {
                                samityStatus = "Samity Canceled";
                            } else if (samityWithEventList.get(dayEndProcessSamityResponse.getSamityId()).contains(SamityEvents.AUTHORIZED.getValue())) {
                                samityStatus = "Completed";
                            } else {
                                samityStatus = "Incomplete";
                            }
                        } else {
                            samityStatus = "No Activity";
                            dayEndProcessSamityResponse.setCollectionAmount(BigDecimal.ZERO);
                            dayEndProcessSamityResponse.setWithdrawAmount(BigDecimal.ZERO);
                            dayEndProcessSamityResponse.setLoanAdjustmentAmount(BigDecimal.ZERO);
                            dayEndProcessSamityResponse.setDisbursementAmount(BigDecimal.ZERO);
//                            dayEndProcessSamityResponse.setFeeCollectionAmount(BigDecimal.ZERO);
                            if (dayEndProcessSamityResponse.getFeeCollectionAmount() != null && dayEndProcessSamityResponse.getFeeCollectionAmount().compareTo(BigDecimal.ZERO) > 0) {
                                samityStatus = "Completed";
                            }
                        }
                        dayEndProcessSamityResponse.setStatus(samityStatus);
                    });
                    return tuples.getT1().stream().sorted(Comparator.comparing(DayEndProcessSamityResponse::getSamityId)).toList();
                });
    }

    private Mono<Map<String, BigDecimal>> getTotalCollectionAmountForSamityResponse(List<String> samityIdList) {
        return collectionStagingDataUseCase.getTotalCollectionAmountForSamityIdList(samityIdList)
                .doOnNext(samityWithTotalCollection -> log.info("Samity List With Total Collection: {}", samityWithTotalCollection));
    }

    private Mono<Map<String, BigDecimal>> getTotalWithdrawAmountForSamityResponse(List<String> samityIdList) {
        return withdrawStagingDataUseCase.getTotalWithdrawAmountForSamityIdList(samityIdList)
                .doOnNext(samityWithTotalWithdraw -> log.info("Samity List With Total Withdraw: {}", samityWithTotalWithdraw));
    }

    private Mono<Map<String, BigDecimal>> getTotalLoanAdjustmentAmountForSamityResponse(List<String> samityIdList) {
        return loanAdjustmentUseCase.getTotalLoanAdjustmentAmountForSamityIdList(samityIdList)
                .doOnNext(samityWithTotalLoanAdjustment -> log.info("Samity List With Total Loan Adjustment: {}", samityWithTotalLoanAdjustment));
    }

    private Mono<Map<String, BigDecimal>> getTotalSavingsAdjustmentAmountForSamityResponse(List<String> samityIdList) {
        return loanAdjustmentUseCase.getTotalSavingsAdjustmentAmountForSamityIdList(samityIdList)
                .doOnNext(samityWithTotalSavingsAdjustment -> log.info("Samity List With Total Savings Adjustment: {}", samityWithTotalSavingsAdjustment));
    }

    private Mono<Map<String, BigDecimal>> getTotalLoanDisbursementAmountForSamityResponse(List<String> samityIdList) {
        return transactionUseCase.getTotalLoanDisbursementAmountForSamityResponse(samityIdList)
                .doOnNext(samityWithTotalLoanDisbursement -> log.info("Samity List With Total Loan Disbursement: {}", samityWithTotalLoanDisbursement));
    }

    private Mono<Map<String, BigDecimal>> getTotalFeeCollectionAmountForSamityResponse(List<String> samityIdList, String officeId) {
        return feeCollectionUseCase.getFeeCollectionByOfficeId(officeId)
                .flatMap(feeCollection -> this.getSamityIdByMemberId(feeCollection.getMemberId())
                        /*.filter(samityIdList::contains)*/
                        .map(samityId -> Map.of(samityId, feeCollection.getAmount())))
                .collectList()
                .map(listOfMaps -> {
                    Map<String, BigDecimal> result = new HashMap<>();
                    listOfMaps.forEach(result::putAll);
                    return result;
                })
                .doOnNext(result -> log.info("Total Fee Collection for Samity Response: {}", result));
    }

    private Mono<String> getSamityIdByMemberId(String memberId) {
        return commonRepository.getMemberOfficeAndSamityEntityByMemberId(memberId)
                .map(MemberAndOfficeAndSamityEntity::getSamityId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT, "No samity id found for member with member id " + memberId)))
                .doOnError(throwable -> log.error("No Samity id found for member with member id : {}", memberId));
    }
}
