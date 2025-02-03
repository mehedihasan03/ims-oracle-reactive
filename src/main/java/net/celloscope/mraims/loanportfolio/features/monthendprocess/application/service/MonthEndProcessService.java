package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.enums.TransactionCodes;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.AccountingUseCase;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.request.AccountingRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaDataEnum;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.CalendarUseCase;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.AccountWithProductEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper.ProductWithAccountId;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.MonthEndProcessPersistenceAdapter;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.MonthEndProcessUseCase;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.request.MonthEndProcessRequestDTO;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.out.MonthEndProcessDataArchivePort;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.out.MonthEndProcessPersistencePort;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessData;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessProductTransaction;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessTracker;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.AccruedInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.SavingsInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response.AccruedInterestDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.AccruedInterest;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.domain.StagingAccountData;
import net.celloscope.mraims.loanportfolio.features.transaction.application.port.in.TransactionUseCase;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MonthEndProcessService implements MonthEndProcessUseCase {
    private final ManagementProcessTrackerUseCase managementProcessUseCase;
    private final OfficeEventTrackerUseCase officeEventUseCase;
    private final SamityEventTrackerUseCase samityEventUseCase;
    private final IStagingDataUseCase stagingDataUseCase;
    private final MonthEndProcessPersistencePort port;
    private final CommonRepository commonRepository;
    private final CalendarUseCase calendarUseCase;
    private final AccruedInterestUseCase accruedInterestUseCase;
    private final AccountingUseCase accountingUseCase;
    private final TransactionUseCase transactionUseCase;
    private final PassbookUseCase passbookUseCase;
    private final ISavingsAccountUseCase savingsAccountUseCase;
    private final MonthEndProcessDataArchivePort monthEndProcessDataArchivePort;
    private final SavingsInterestUseCase savingsInterestUseCase;
    private final TransactionalOperator rxtx;
    private final Gson gson;
    private final MonthEndProcessPersistenceAdapter monthEndProcessPersistenceAdapter;

    public MonthEndProcessService(ManagementProcessTrackerUseCase managementProcessUseCase, OfficeEventTrackerUseCase officeEventUseCase, SamityEventTrackerUseCase samityEventUseCase, IStagingDataUseCase stagingDataUseCase, MonthEndProcessPersistencePort port, CommonRepository commonRepository, CalendarUseCase calendarUseCase, AccruedInterestUseCase accruedInterestUseCase, AccountingUseCase accountingUseCase, TransactionUseCase transactionUseCase, PassbookUseCase passbookUseCase, ISavingsAccountUseCase savingsAccountUseCase, MonthEndProcessDataArchivePort monthEndProcessDataArchivePort, SavingsInterestUseCase savingsInterestUseCase, TransactionalOperator rxtx, MonthEndProcessPersistenceAdapter monthEndProcessPersistenceAdapter) {
        this.managementProcessUseCase = managementProcessUseCase;
        this.officeEventUseCase = officeEventUseCase;
        this.samityEventUseCase = samityEventUseCase;
        this.stagingDataUseCase = stagingDataUseCase;
        this.port = port;
        this.commonRepository = commonRepository;
        this.calendarUseCase = calendarUseCase;
        this.accruedInterestUseCase = accruedInterestUseCase;
        this.accountingUseCase = accountingUseCase;
        this.transactionUseCase = transactionUseCase;
        this.passbookUseCase = passbookUseCase;
        this.savingsAccountUseCase = savingsAccountUseCase;
        this.monthEndProcessDataArchivePort = monthEndProcessDataArchivePort;
        this.savingsInterestUseCase = savingsInterestUseCase;
        this.rxtx = rxtx;
        this.gson = CommonFunctions.buildGson(this);
        this.monthEndProcessPersistenceAdapter = monthEndProcessPersistenceAdapter;
    }

    @Override
    public Mono<MonthEndProcessGridViewResponseDTO> gridViewOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .map(managementProcessTracker -> MonthEndProcessGridViewResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcessTracker.getOfficeNameEn())
                        .officeNameBn(managementProcessTracker.getOfficeNameBn())
                        .businessDate(managementProcessTracker.getBusinessDate())
                        .businessDay(managementProcessTracker.getBusinessDay())
                        .build())
                .flatMap(responseDTO -> this.getMonthEndProcessHistoryListForOffice(requestDTO.getOfficeId(), requestDTO.getLimit(), requestDTO.getOffset())
                        .map(historyList -> {
                            responseDTO.setData(historyList);
                            responseDTO.setTotalCount(historyList.size());
                            return responseDTO;
                        }))
                .flatMap(responseDTO -> this.setBtnStatusAndStatusForGridViewOfMonthEndProcess(managementProcess.get(), responseDTO))
                .flatMap(responseDTO -> this.getFinancialPeriodAvailability(responseDTO.getOfficeId())
                        .map(financialPeriodAvailability -> {
                            responseDTO.setIsFinancialPeriodAvailable(financialPeriodAvailability);
                            return responseDTO;
                        }))
                .doOnError(throwable -> log.error("Error in Month End Process History Grid View: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Month End Process History Grid View Response: {}", response));
    }

    private Mono<String> getFinancialPeriodAvailability(String officeId) {
        return commonRepository.getFinancialPeriodEntriesForOffice(officeId)
                .filter(financialPeriod -> financialPeriod.getStatus().equals(Status.STATUS_OPENED.getValue()))
                .collectList()
                .map(financialPeriodList -> financialPeriodList.isEmpty() ? "No" : "Yes");
    }

    @Override
    public Mono<MonthEndProcessStatusGridViewResponseDTO> gridViewOfStatusOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .map(managementProcessTracker -> {
                    managementProcess.set(managementProcessTracker);
                    return managementProcessTracker;
                })
                .map(managementProcessTracker -> gson.fromJson(managementProcessTracker.toString(), MonthEndProcessStatusGridViewResponseDTO.class))
                .flatMap(responseDTO -> this.getSamityStatusResponseForMonthEndProcessStatusGridView(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId())
                        .flatMap(samityStatusResponseList -> this.getMonthEndProcessDataAndBuildSamityStatusResponse(samityStatusResponseList, managementProcess.get().getManagementProcessId(), responseDTO.getOfficeId()))
                        .map(samityStatusResponseList -> {
                            responseDTO.setData(samityStatusResponseList);
                            responseDTO.setTotalCount(samityStatusResponseList.size());
                            return responseDTO;
                        }))
                .flatMap(responseDTO -> this.setStatusAndBtnStatusForMonthEndProcessStatusGridView(responseDTO, managementProcess.get()))
                .flatMap(responseDTO -> this.getFinancialPeriodAvailability(responseDTO.getOfficeId())
                        .map(financialPeriodAvailability -> {
                            responseDTO.setIsFinancialPeriodAvailable(financialPeriodAvailability);
                            return responseDTO;
                        }))
                .doOnError(throwable -> log.error("Error in Month End Process Status Grid View: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Month End Process Status Grid View Response: {}", response));
    }

    @Override
    public Mono<MonthEndProcessStatusGridViewResponseDTO> runSamityStatusOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(this::checkIfMonthEndProcessSamityStatusIsRunnable)
                .map(managementProcessTracker -> gson.fromJson(managementProcessTracker.toString(), MonthEndProcessStatusGridViewResponseDTO.class))
                .flatMap(responseDTO -> this.getSamityStatusResponseForMonthEndProcessStatusGridView(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId())
                        .map(samityStatusResponseList -> {
                            samityStatusResponseList.forEach(samityStatusResponse -> {
                                samityStatusResponse.setStatus(Status.STATUS_WAITING.getValue());
                                samityStatusResponse.setTotalAccruedAmount(BigDecimal.ZERO);
                                samityStatusResponse.setTotalPostingAmount(BigDecimal.ZERO);
                            });
                            responseDTO.setData(samityStatusResponseList);
                            responseDTO.setTotalCount(samityStatusResponseList.size());
                            return responseDTO;
                        }))
                .flatMap(responseDTO -> this.setStatusAndBtnStatusForMonthEndProcessStatusGridView(responseDTO, managementProcess.get()))
//                .doOnNext(responseDTO -> this.runMonthEndProcessSamityListForAccrualAndPosting(responseDTO.getOfficeId(), responseDTO.getData().stream().map(MonthEndProcessSamityStatusResponse::getSamityId).toList(), requestDTO.getLoginId()))
                .flatMap(responseDTO -> Mono.deferContextual(contextView ->
                                Mono.fromRunnable(() -> {
                                    Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                                    this.runMonthEndProcessSamityListForAccrualAndPosting(responseDTO.getOfficeId(), responseDTO.getData().stream().map(MonthEndProcessSamityStatusResponse::getSamityId).toList(), requestDTO.getLoginId())
                                            .contextWrite(context)
                                            .subscribeOn(Schedulers.immediate())
                                            .subscribe();
                                })
                                .thenReturn(responseDTO)))
                .doOnError(throwable -> log.error("Error Running Month End Process Status: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Running Month End Process Status Response: {}", response));
    }

    @Override
    public Mono<MonthEndProcessStatusGridViewResponseDTO> retrySamityStatusOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> this.checkIfMonthEndProcessSamityListIsRetryable(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId(), requestDTO.getSamityIdList()))
                .map(data -> MonthEndProcessStatusGridViewResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcess.get().getOfficeNameEn())
                        .officeNameBn(managementProcess.get().getOfficeNameBn())
                        .businessDate(managementProcess.get().getBusinessDate())
                        .businessDay(managementProcess.getAcquire().getBusinessDay())
                        .status(Status.STATUS_PROCESSING.getValue())
                        .userMessage("Month End Process for Samity List Retry is Started for Office: " + requestDTO.getOfficeId())
                        .btnStartProcessEnabled("No")
                        .btnRefreshEnabled("Yes")
                        .btnAccountingEnabled("No")
                        .samityIdList(requestDTO.getSamityIdList())
                        .totalCount(data.size())
                        .build())
//                .doOnNext(responseDTO -> this.retryMonthEndProcessSamityListForAccrualAndPosting(responseDTO.getOfficeId(), responseDTO.getSamityIdList(), requestDTO.getLoginId()))
                .flatMap(responseDTO -> Mono.deferContextual(contextView ->
                                Mono.fromRunnable(() -> {
                                    Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                                    this.retryMonthEndProcessSamityListForAccrualAndPosting(responseDTO.getOfficeId(), responseDTO.getSamityIdList(), requestDTO.getLoginId())
                                            .contextWrite(context)
                                            .subscribeOn(Schedulers.immediate())
                                            .subscribe();
                                })
                                .thenReturn(responseDTO))
                )
                .doOnError(throwable -> log.error("Error Running Month End Process Samity List Retry: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Running Month End Process Samity List Retry Response: {}", response));
    }

    private Mono<Void> retryMonthEndProcessSamityListForAccrualAndPosting(String officeId, List<String> samityIdList, String loginId) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(officeId)
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> this.retryAndRunMonthEndProcessForSamityAccrualAndPosting(managementProcess.get(), samityIdList, loginId))
                .doOnSuccess(response -> log.info("Month End Process Samity Accrual And Posting Completed Successfully for Office: {}", officeId))
                .doOnError(throwable -> log.error("Error on Running Month End Process Samity Accrual And Posting for office: {}, Error Message: {}", officeId, throwable.getMessage()))
               /* .subscribeOn(Schedulers.immediate())
                .subscribe();*/
                .then();
    }

    private Mono<?> retryAndRunMonthEndProcessForSamityAccrualAndPosting(ManagementProcessTracker managementProcessTracker, List<String> samityIdList, String loginId) {
        return Flux.fromIterable(samityIdList)
                .flatMap(samityId -> port.updateMonthEndProcessDataForRetry(managementProcessTracker.getManagementProcessId(), samityId, loginId))
                .flatMap(monthEndProcessData -> this.updateAndRunMonthEndProcessForOneSamityAccrualAndPosting(monthEndProcessData, managementProcessTracker.getBusinessDate()))
                .doOnNext(monthEndProcessData -> log.info("Samity {} Month End Accrual And Posting Completed Successfully", monthEndProcessData.getSamityId()))
                .collectList();
    }

    @Override
    public Mono<MonthEndProcessStatusGridViewResponseDTO> retryAllSamityStatusOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> this.checkIfMonthEndProcessIsRetryableForOffice(managementProcessTracker.getManagementProcessId(), requestDTO.getOfficeId()))
                .map(samityIdList -> MonthEndProcessStatusGridViewResponseDTO.builder()
                        .officeId(requestDTO.getOfficeId())
                        .officeNameEn(managementProcess.get().getOfficeNameEn())
                        .officeNameBn(managementProcess.get().getOfficeNameBn())
                        .businessDate(managementProcess.get().getBusinessDate())
                        .businessDay(managementProcess.getAcquire().getBusinessDay())
                        .status(Status.STATUS_PROCESSING.getValue())
                        .userMessage("Month End Process Data Retry is Started for Office: " + requestDTO.getOfficeId())
                        .btnStartProcessEnabled("No")
                        .btnRefreshEnabled("Yes")
                        .btnAccountingEnabled("No")
                        .samityIdList(samityIdList)
                        .totalCount(samityIdList.size())
                        .build())
                .doOnNext(responseDTO -> this.retryMonthEndProcessSamityListForAccrualAndPosting(responseDTO.getOfficeId(), responseDTO.getSamityIdList(), requestDTO.getLoginId()))
                .doOnError(throwable -> log.error("Error Running Month End Process Retry for Office: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Running Month End Process Data Retry for Office Response: {}", response));
    }

    private Mono<List<String>> checkIfMonthEndProcessIsRetryableForOffice(String managementProcessId, String officeId) {
        return port.getMonthEndProcessDataEntriesForOffice(managementProcessId, officeId)
                .collectList()
                .filter(monthEndProcessDataList -> !monthEndProcessDataList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process is Not Started yet for Office")))
                .map(monthEndProcessDataList -> monthEndProcessDataList.stream()
                        .filter(monthEndProcessData -> monthEndProcessData.getStatus().equals(Status.STATUS_FAILED.getValue()))
                        .map(MonthEndProcessData::getSamityId)
                        .toList())
                .filter(retryableSamityIdList -> !retryableSamityIdList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process has No Retryable Samity for Office")));
    }

    private Mono<List<String>> checkIfMonthEndProcessSamityListIsRetryable(String managementProcessId, String officeId, List<String> samityIdList) {
        return port.getMonthEndProcessDataEntriesForOffice(managementProcessId, officeId)
                .collectList()
                .filter(monthEndProcessDataList -> !monthEndProcessDataList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process is Not Started yet for Office")))
                .map(monthEndProcessDataList -> monthEndProcessDataList.stream()
                        .filter(monthEndProcessData -> samityIdList.contains(monthEndProcessData.getSamityId()))
                        .filter(monthEndProcessData -> monthEndProcessData.getStatus().equals(Status.STATUS_FAILED.getValue()))
                        .map(MonthEndProcessData::getSamityId)
                        .toList())
                .filter(retryableSamityIdList -> !retryableSamityIdList.isEmpty() && retryableSamityIdList.size() == samityIdList.size())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process Samity List is Not Retryable for Office")));
    }

    private Mono<Void> runMonthEndProcessSamityListForAccrualAndPosting(String officeId, List<String> samityIdList, String loginId) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        AtomicReference<String> monthEndProcessId = new AtomicReference<>(UUID.randomUUID().toString());
        return managementProcessUseCase.getLastManagementProcessForOffice(officeId)
                .doOnNext(managementProcess::set)
                .flatMap(managementProcessTracker -> this.updateAndRunMonthEndProcessForSamityAccrualAndPosting(managementProcess.get(), monthEndProcessId.get(), officeId, samityIdList, loginId))
                .doOnSuccess(response -> log.info("Month End Process Samity Accrual And Posting Completed Successfully for Office: {}", officeId))
                .doOnError(throwable -> log.error("Error on Running Month End Process Samity Accrual And Posting for office: {}, Error Message: {}", officeId, throwable.getMessage()))
               /* .subscribeOn(Schedulers.immediate())
                .subscribe();*/
                .then();
    }

    private Mono<List<MonthEndProcessData>> updateAndRunMonthEndProcessForSamityAccrualAndPosting(ManagementProcessTracker managementProcessTracker, String monthEndProcessId, String officeId, List<String> samityIdList, String loginId) {
        List<MonthEndProcessData> monthEndProcessDataList = samityIdList.stream()
                .map(samityId -> MonthEndProcessData.builder()
                        .managementProcessId(managementProcessTracker.getManagementProcessId())
                        .monthEndProcessTrackerId(monthEndProcessId)
                        .officeId(officeId)
                        .samityId(samityId)
                        .totalAccruedAmount(BigDecimal.ZERO)
                        .totalPostingAmount(BigDecimal.ZERO)
                        .status(Status.STATUS_WAITING.getValue())
                        .createdOn(LocalDateTime.now())
                        .createdBy(loginId)
                        .build())
                .toList();
        return port.insertMonthEndProcessDataList(monthEndProcessDataList)
                .flatMapIterable(updatedMonthEndProcessDataList -> updatedMonthEndProcessDataList)
                .delayElements(Duration.ofSeconds(2))
                .flatMap(monthEndProcessData -> this.updateAndRunMonthEndProcessForOneSamityAccrualAndPosting(monthEndProcessData, managementProcessTracker.getBusinessDate()))
                .doOnNext(monthEndProcessData -> log.info("Samity {} Month End Accrual And Posting Completed Successfully", monthEndProcessData.getSamityId()))
                .collectList();
    }

    private Mono<MonthEndProcessData> updateAndRunMonthEndProcessForOneSamityAccrualAndPosting(MonthEndProcessData monthEndProcessData, LocalDate businessDate) {
        log.info("TEST || Month End Process Data for Samity: {} is: {}", monthEndProcessData.getSamityId(), monthEndProcessData);
        return port.updateMonthEndProcessDataForProcessing(monthEndProcessData)
                .flatMap(monthEndData -> this.runAccrualAndPostingForOneSamity(monthEndData, businessDate))
                .flatMap(accruedInterestDTO -> {
                    if (accruedInterestDTO.getTotalInterestAccrued().compareTo(BigDecimal.ZERO) >= 0 /*&& accruedInterestDTO.getTotalInterestPosted().compareTo(BigDecimal.ZERO) >= 0*/) {
                        return port.updateMonthEndProcessDataForTotalAccruedAndPostingAmount(monthEndProcessData, accruedInterestDTO.getTotalInterestAccrued(), accruedInterestDTO.getTotalInterestPosted());
                    }
                    String remarks = "Month End Process Accrual And Posting for Samity Failed";
                    return port.updateMonthEndProcessDataForFailed(monthEndProcessData, remarks);
                })
                .flatMap(monthEndData -> {
                    if (!monthEndData.getStatus().equals(Status.STATUS_FAILED.getValue())) {
                        return port.updateMonthEndProcessDataForFinished(monthEndProcessData);
                    }
                    return Mono.just(monthEndData);
                });
    }

    private Mono<AccruedInterestDTO> runAccrualAndPostingForOneSamity(MonthEndProcessData monthEndProcessData, LocalDate businessDate) {
        return stagingDataUseCase.getAllStagingAccountDataBySamityIdList(List.of(monthEndProcessData.getSamityId()))
                .map(stagingAccountDataList -> stagingAccountDataList.stream()
                        .map(StagingAccountData::getSavingsAccountId)
                        .filter(savingsAccountId -> !HelperUtil.checkIfNullOrEmpty(savingsAccountId))
                        .toList())
                .doOnNext(savingsAccountIdList -> log.info("Savings Account Id List for Samity: {} is: {}", monthEndProcessData.getSamityId(), savingsAccountIdList))
                .flatMap(savingsAccountIdList -> accruedInterestUseCase.accrueAndSaveMonthlyInterest(savingsAccountIdList, businessDate.getMonthValue(), businessDate.getYear(), monthEndProcessData.getCreatedBy(), monthEndProcessData.getManagementProcessId(), monthEndProcessData.getMonthEndProcessTrackerId(), monthEndProcessData.getSamityId(), monthEndProcessData.getOfficeId(), businessDate))
                .doOnNext(accruedInterestDTO -> log.info("Accrued Interest DTO for Samity: {} is: {}", monthEndProcessData.getSamityId(), accruedInterestDTO))
                .doOnError(throwable -> log.error("Error in Running Accrual And Posting for Samity: {}, Error Message: {}", monthEndProcessData.getSamityId(), throwable.getMessage()))
                .onErrorReturn(AccruedInterestDTO.builder()
                        .totalInterestAccrued(BigDecimal.valueOf(-1))
                        .totalInterestPosted(BigDecimal.valueOf(-1))
                        .build());
    }

    private Mono<BigDecimal> runPostingForMonthEndProcessForOneSamity(MonthEndProcessData monthEndProcessData) {
        return Mono.just(monthEndProcessData.getSamityId())
                .delayElement(Duration.ofSeconds(1))
                .map(samityId -> {
                    if (samityId.equals("1018-120")) {
                        return BigDecimal.valueOf(-1);
                    }
                    return BigDecimal.valueOf(100.00);
                });
    }

    private Mono<BigDecimal> runAccrualForMonthEndProcessForOneSamity(MonthEndProcessData monthEndProcessData) {
        return Mono.just(monthEndProcessData.getSamityId())
                .delayElement(Duration.ofSeconds(1))
                .map(samityId -> {
                    if (samityId.equals("1018-120")) {
                        return BigDecimal.valueOf(-1);
                    }
                    return BigDecimal.valueOf(100.0);
                });
    }


    private Mono<ManagementProcessTracker> checkIfMonthEndProcessSamityStatusIsRunnable(ManagementProcessTracker managementProcessTracker) {
        return this.validateIfMonthEndProcessIsRunnableTodayForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), managementProcessTracker.getBusinessDate())
                .flatMap(officeEventList -> port.getMonthEndProcessDataEntriesForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .collectList())
                .filter(List::isEmpty)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process is Already Processing for Office")))
                .map(data -> managementProcessTracker);
    }

    private Mono<Boolean> validateIfMonthEndProcessIsRunnableTodayForOffice(String managementProcessId, String officeId, LocalDate businessDate) {
        return calendarUseCase.getLastWorkingDayOfAMonthOfCurrentYearForOffice(officeId, businessDate)
                .doOnNext(monthEndDate -> log.info("Office: {} Month: {}, Current Business Date: {}, Month End Date: {}", officeId, businessDate.getMonth(), businessDate, monthEndDate))
                .flatMap(monthEndDate -> {
                    if (!businessDate.equals(monthEndDate)) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process Cannot be Run today. Next Month End Process Date is " + monthEndDate + " for Office"));
                    }
                    return Mono.just(monthEndDate);
                })
                .flatMap(monthEndDate -> officeEventUseCase.getAllOfficeEventsForOffice(managementProcessId, officeId)
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList())
                .filter(officeEventList -> officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process is Not Completed for Office")))
                .filter(officeEventList -> officeEventList.stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process is Already Completed for Office")))
                .map(officeEventList -> true);

    }

    private Mono<MonthEndProcessStatusGridViewResponseDTO> setStatusAndBtnStatusForMonthEndProcessStatusGridView(MonthEndProcessStatusGridViewResponseDTO responseDTO, ManagementProcessTracker managementProcessTracker) {
        return calendarUseCase.getLastWorkingDayOfAMonthOfCurrentYearForOffice(managementProcessTracker.getOfficeId(), managementProcessTracker.getBusinessDate())
                .flatMap(nextBusinessDate -> {
                    if (managementProcessTracker.getBusinessDate().equals(nextBusinessDate)) {
                        return this.getOfficeEventsAndSetBtnStatusAndStatusForMonthEndProcessStatusGridView(managementProcessTracker, responseDTO);
                    }
                    responseDTO.setBtnStartProcessEnabled("No");
                    responseDTO.setBtnRefreshEnabled("No");
                    responseDTO.setBtnAccountingEnabled("No");
                    responseDTO.setBtnDeleteEnabled("No");
                    responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                    responseDTO.setUserMessage("Next Month End Process Date is " + nextBusinessDate + " for this Month of Office");
                    return Mono.just(responseDTO);
                });
    }

    private Mono<MonthEndProcessStatusGridViewResponseDTO> getOfficeEventsAndSetBtnStatusAndStatusForMonthEndProcessStatusGridView(ManagementProcessTracker managementProcessTracker, MonthEndProcessStatusGridViewResponseDTO responseDTO) {
        return officeEventUseCase.getAllOfficeEventsForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .map(OfficeEventTracker::getOfficeEvent)
                .collectList()
                .doOnNext(officeEventList -> log.info("Office Event List for Office {} is: {}", responseDTO.getOfficeId(), officeEventList))
                .flatMap(officeEventList -> {
                    if (officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))) {
                        if (officeEventList.stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue()))) {
                            if (!responseDTO.getData().isEmpty()) {
                                responseDTO.setBtnStartProcessEnabled("No");
                                responseDTO.setBtnRefreshEnabled("Yes");
                                responseDTO.setBtnDeleteEnabled("No");
                                if (responseDTO.getData().stream().allMatch(samityResponse -> samityResponse.getStatus().equals(Status.STATUS_FINISHED.getValue()))) {
                                    responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                                    responseDTO.setUserMessage("Month End Process Samity Accrual And Posting is Completed for this Month of Office");
                                    responseDTO.setBtnAccountingEnabled("Yes");
                                } else {
                                    responseDTO.setBtnAccountingEnabled("No");
                                    responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                                    responseDTO.setUserMessage("Month End Process Samity Accrual And Posting is Processing for this Month of Office");
                                }
                                return Mono.just(responseDTO);
                            }
                            responseDTO.setBtnStartProcessEnabled("Yes");
                            responseDTO.setBtnRefreshEnabled("No");
                            responseDTO.setBtnAccountingEnabled("No");
                            responseDTO.setBtnDeleteEnabled("No");
                            responseDTO.setStatus(Status.STATUS_WAITING.getValue());
                            responseDTO.setUserMessage("Month End Process is Not Completed for this Month of Office");
                            return Mono.just(responseDTO);
                        }
                        responseDTO.setBtnStartProcessEnabled("No");
                        responseDTO.setBtnRefreshEnabled("Yes");
                        responseDTO.setBtnAccountingEnabled("Yes");
                        responseDTO.setBtnDeleteEnabled("Yes");
                        responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                        responseDTO.setUserMessage("Month End Process is Completed for this Month of Office");
                        return Mono.just(responseDTO);
                    }
                    responseDTO.setBtnStartProcessEnabled("No");
                    responseDTO.setBtnRefreshEnabled("No");
                    responseDTO.setBtnAccountingEnabled("No");
                    responseDTO.setBtnDeleteEnabled("No");
                    responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                    responseDTO.setUserMessage("Day End Process is Not Completed for Office. Complete Day End Process First");
                    return Mono.just(responseDTO);
                });
    }

    private Mono<List<MonthEndProcessSamityStatusResponse>> getSamityStatusResponseForMonthEndProcessStatusGridView(String managementProcessId, String officeId) {
        return stagingDataUseCase.getStagingProcessEntityByOffice(managementProcessId, officeId)
                .map(stagingProcessTrackerEntity -> {
                    MonthEndProcessSamityStatusResponse samityStatusResponse = gson.fromJson(stagingProcessTrackerEntity.toString(), MonthEndProcessSamityStatusResponse.class);
                    samityStatusResponse.setStatus(null);
                    samityStatusResponse.setProcessStartTime(null);
                    samityStatusResponse.setProcessEndTime(null);
                    return samityStatusResponse;
                })
                .sort(Comparator.comparing(MonthEndProcessSamityStatusResponse::getSamityId))
                .collectList();
    }

    private Mono<List<MonthEndProcessSamityStatusResponse>> getMonthEndProcessDataAndBuildSamityStatusResponse(List<MonthEndProcessSamityStatusResponse> samityStatusResponseList, String managementProcessId, String officeId) {
        return port.getMonthEndProcessDataEntriesForOffice(managementProcessId, officeId)
            .doOnNext(monthEndProcessData -> log.info("TEST | Month End Process Data: {}", monthEndProcessData))
            .collectList()
                .map(monthEndProcessDataList -> {
                    if (!monthEndProcessDataList.isEmpty()) {
                        return this.buildSamityStatusResponseForMonthEndProcess(samityStatusResponseList, monthEndProcessDataList);
                    }
                    samityStatusResponseList.clear();
                    return samityStatusResponseList;
                });
    }

    private List<MonthEndProcessSamityStatusResponse> buildSamityStatusResponseForMonthEndProcess(List<MonthEndProcessSamityStatusResponse> samityStatusResponseList, List<MonthEndProcessData> monthEndProcessDataList) {
        samityStatusResponseList.forEach(samityStatusResponse -> {
            monthEndProcessDataList.forEach(monthEndProcessData -> {
                if (monthEndProcessData.getSamityId().equals(samityStatusResponse.getSamityId())) {
                    log.info("Month End Process Samity Response: {}", monthEndProcessData);
                    samityStatusResponse.setTotalAccruedAmount(monthEndProcessData.getTotalAccruedAmount());
                    samityStatusResponse.setTotalPostingAmount(monthEndProcessData.getTotalPostingAmount());
                    samityStatusResponse.setStatus(monthEndProcessData.getStatus());
                    samityStatusResponse.setProcessStartTime(monthEndProcessData.getProcessStartTime());
                    samityStatusResponse.setProcessEndTime(monthEndProcessData.getProcessEndTime());
                    if (samityStatusResponse.getStatus().equals(Status.STATUS_FAILED.getValue())) {
                        samityStatusResponse.setBtnRetryEnabled("Yes");
                    } else {
                        samityStatusResponse.setBtnRetryEnabled("No");
                    }
                } else if (monthEndProcessDataList.stream().noneMatch(monthEndData -> monthEndData.getSamityId().equals(samityStatusResponse.getSamityId()))) {
                    samityStatusResponse.setTotalAccruedAmount(BigDecimal.ZERO);
                    samityStatusResponse.setTotalPostingAmount(BigDecimal.ZERO);
                    samityStatusResponse.setStatus(Status.STATUS_WAITING.getValue());
                    samityStatusResponse.setBtnRetryEnabled("No");
                }
            });
        });
        return samityStatusResponseList.stream().sorted(Comparator.comparing(MonthEndProcessSamityStatusResponse::getSamityId)).toList();
    }

    @Override
    public Mono<MonthEndProcessAccountingViewResponseDTO> gridViewOfAccountingOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .map(managementProcessTracker -> gson.fromJson(managementProcessTracker.toString(), MonthEndProcessAccountingViewResponseDTO.class))
                .flatMap(responseDTO -> this.setBtnStatusForAccountingOfMonthEndProcessGridView(managementProcess.get().getManagementProcessId(), responseDTO))
                .flatMap(responseDTO -> this.buildMonthEndProcessAccountingGridViewResponse(managementProcess.get().getManagementProcessId(), responseDTO))
                .flatMap(responseDTO -> this.getFinancialPeriodAvailability(responseDTO.getOfficeId())
                        .map(financialPeriodAvailability -> {
                            responseDTO.setIsFinancialPeriodAvailable(financialPeriodAvailability);
                            return responseDTO;
                        }))
                .doOnError(throwable -> log.error("Error in Grid View of Accounting of Month End Process: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Grid View of Accounting of Month End Process Response: {}", response));
    }

    private Mono<MonthEndProcessAccountingViewResponseDTO> setBtnStatusForAccountingOfMonthEndProcessGridView(String managementProcessId, MonthEndProcessAccountingViewResponseDTO responseDTO) {
        return officeEventUseCase.getAllOfficeEventsForOffice(managementProcessId, responseDTO.getOfficeId())
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .map(OfficeEventTracker::getOfficeEvent)
                .collectList()
                .flatMap(officeEventList -> {
                    if (officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))) {
                        if (officeEventList.stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue()))) {
                            return this.getMonthEndProcessDataAndSetBtnStatusForAccounting(managementProcessId, responseDTO);
                        }
                        responseDTO.setBtnConfirmEnabled("No");
                        responseDTO.setBtnRefreshEnabled("Yes");
                        responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                        responseDTO.setUserMessage("Month End Process Accounting is Completed for Office");
                        return Mono.just(responseDTO);
                    }
                    responseDTO.setBtnConfirmEnabled("No");
                    responseDTO.setBtnRefreshEnabled("No");
                    responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                    responseDTO.setUserMessage("Month End Process Accounting Cannot be Run today");
                    return Mono.just(responseDTO);
                });
    }

    private Mono<MonthEndProcessAccountingViewResponseDTO> getMonthEndProcessDataAndSetBtnStatusForAccounting(String managementProcessId, MonthEndProcessAccountingViewResponseDTO responseDTO) {
        return port.getMonthEndProcessTrackerEntriesByManagementProcessForOffice(managementProcessId, responseDTO.getOfficeId())
                .filter(monthEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(monthEndProcessTracker.getTransactionCode()))
                .collectList()
                .flatMap(monthEndProcessTrackerList -> {
                    if (monthEndProcessTrackerList.isEmpty()) {
                        return port.getMonthEndProcessDataEntriesForOffice(managementProcessId, responseDTO.getOfficeId())
                                .collectList()
                                .map(monthEndProcessDataList -> {
                                    if (!monthEndProcessDataList.isEmpty()) {
                                        responseDTO.setBtnConfirmEnabled("Yes");
                                        responseDTO.setBtnRefreshEnabled("No");
                                        responseDTO.setStatus(Status.STATUS_WAITING.getValue());
                                        responseDTO.setUserMessage("Month End Process Accounting is not Completed For Office");
                                    } else {
                                        responseDTO.setBtnConfirmEnabled("No");
                                        responseDTO.setBtnRefreshEnabled("No");
                                        responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                                        responseDTO.setUserMessage("Month End Process Accounting is not Completed For Office");
                                    }
                                    return responseDTO;
                                });
                    }
                    responseDTO.setBtnConfirmEnabled("No");
                    responseDTO.setBtnRefreshEnabled("Yes");
                    responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                    responseDTO.setUserMessage("Month End Process Accounting is Processing For Office");
                    return Mono.just(responseDTO);
                });
    }

    @Override
    public Mono<MonthEndProcessAccountingViewResponseDTO> runAccountingOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(this::checkIfAccountingOfMonthEndProcessIsValid)
                .map(managementProcessTracker -> gson.fromJson(managementProcessTracker.toString(), MonthEndProcessAccountingViewResponseDTO.class))
                .flatMap(responseDTO -> this.buildMonthEndProcessAccountingGridViewResponse(managementProcess.get().getManagementProcessId(), responseDTO))
                .map(responseDTO -> {
                    responseDTO.setBtnConfirmEnabled("No");
                    responseDTO.setBtnRefreshEnabled("Yes");
                    responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                    responseDTO.setUserMessage("Month End Process Accounting is Started for Office");
                    return responseDTO;
                })
//                .doOnNext(responseDTO -> this.getAccountingAndUpdateMonthEndProcessTrackerForOffice(managementProcess.get(), responseDTO, requestDTO.getLoginId()))
                .flatMap(responseDTO -> Mono.deferContextual(contextView ->
                        Mono.fromRunnable(() -> {
                            Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                            this.getAccountingAndUpdateMonthEndProcessTrackerForOffice(managementProcess.get(), responseDTO, requestDTO.getLoginId())
                                    .contextWrite(context)
                                    .subscribeOn(Schedulers.immediate())
                                    .subscribe();
                            })
                        .thenReturn(responseDTO)))
                .doOnError(throwable -> log.error("Error in Accounting Month End Process: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Accounting of Month End Process Response: {}", response));
    }

    @Override
    public Mono<MonthEndProcessAccountingViewResponseDTO> retryAccountingByTransactionCodeListOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(ManagementProcessTracker -> this.checkIfAccountingProcessIsRetryableByTransactionCodeListForOffice(ManagementProcessTracker, requestDTO.getTransactionCodeList()))
                .map(managementProcessTracker -> gson.fromJson(managementProcessTracker.toString(), MonthEndProcessAccountingViewResponseDTO.class))
                .map(responseDTO -> {
                    responseDTO.setBtnConfirmEnabled("No");
                    responseDTO.setBtnRefreshEnabled("Yes");
                    responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                    responseDTO.setUserMessage("Month End Accounting Retry Process by Transaction Code is Started for Office");
                    return responseDTO;
                })
//                .doOnNext(responseDTO -> this.retryAndGetAccountingAndUpdateMonthEndProcessTrackerByTransactionCodeForOffice(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId(), requestDTO.getTransactionCodeList(), requestDTO.getLoginId()))
                .flatMap(responseDTO -> Mono.deferContextual(contextView ->
                        Mono.fromRunnable(() -> {
                            Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                            this.retryAndGetAccountingAndUpdateMonthEndProcessTrackerByTransactionCodeForOffice(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId(), requestDTO.getTransactionCodeList(), requestDTO.getLoginId())
                                    .contextWrite(context)
                                    .subscribeOn(Schedulers.immediate())
                                    .subscribe();
                        })
                        .thenReturn(responseDTO))
                )
                .doOnError(throwable -> log.error("Error in Accounting Retry by Transaction Code List of Month End Process: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Accounting Retry by Transaction Code List of Month End Process Response: {}", response));
    }

    @Override
    public Mono<MonthEndProcessAccountingViewResponseDTO> retryAllAccountingOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMap(this::checkIfAccountingProcessIsRetryableForOffice)
                .map(managementProcessTracker -> gson.fromJson(managementProcessTracker.toString(), MonthEndProcessAccountingViewResponseDTO.class))
                .map(responseDTO -> {
                    responseDTO.setBtnConfirmEnabled("No");
                    responseDTO.setBtnRefreshEnabled("Yes");
                    responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                    responseDTO.setUserMessage("Month End Accounting Retry Process is Started for Office");
                    return responseDTO;
                })
//                .doOnNext(responseDTO -> this.retryAndGetAccountingAndUpdateMonthEndProcessTrackerForOffice(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId(), requestDTO.getLoginId()))
                .flatMap(responseDTO -> Mono.deferContextual(contextView ->
                        Mono.fromRunnable(() -> {
                            Context context = Context.of(contextView.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                            this.retryAndGetAccountingAndUpdateMonthEndProcessTrackerForOffice(managementProcess.get().getManagementProcessId(), requestDTO.getOfficeId(), requestDTO.getLoginId())
                                    .contextWrite(context)
                                    .subscribeOn(Schedulers.immediate())
                                    .subscribe();
                        })
                        .thenReturn(responseDTO))
                )
                .doOnError(throwable -> log.error("Error in Accounting Retry by Office of Month End Process: {}", throwable.getMessage()))
                .doOnSuccess(response -> log.info("Accounting Retry by Office of Month End Process Response: {}", response));
    }

    @Override
    public Mono<MonthEndProcessResponseDTO> revertMonthEndProcess(MonthEndProcessRequestDTO requestDTO) {
        AtomicReference<ManagementProcessTracker> managementProcessTrackerRef = new AtomicReference<>();
        AtomicReference<OfficeEventTracker> officeEventTrackerRef = new AtomicReference<>();
        return getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .doOnNext(managementProcessTrackerRef::set)
                .flatMap(managementProcessTracker ->  getLastOfficeEventForOffice(managementProcessTrackerRef.get().getManagementProcessId(), requestDTO.getOfficeId()))
                .flatMap(officeEventTracker -> {
                            officeEventTrackerRef.set(officeEventTracker);
                            return this.validateIfMonthEndProcessIsRevertibleForOffice(officeEventTracker, managementProcessTrackerRef.get());
                        }
                )
                .flatMap(response -> revertMonthEndProcessSteps(managementProcessTrackerRef.get(), officeEventTrackerRef.get()))
                .as(rxtx::transactional)
                .then(Mono.just(MonthEndProcessResponseDTO
                        .builder()
                                .officeId(requestDTO.getOfficeId())
                                .status(Status.STATUS_FINISHED.getValue())
                                .userMessage("Month End Process is Reverted Successfully")
                        .build())
                );
    }

    private Mono<String> revertMonthEndProcessSteps(ManagementProcessTracker managementProcessTracker, OfficeEventTracker officeEventTracker) {
        return updateSavingsAccountBalanceFromPassBookDepositAmount(managementProcessTracker.getManagementProcessId())
                .then(saveAndDeletePassBookData(managementProcessTracker.getManagementProcessId()))
                .then(saveAndDeleteTransactionData(managementProcessTracker.getManagementProcessId()))
                .then(saveAndDeleteMonthEndProcessTracker(managementProcessTracker.getManagementProcessId()))
                .then(saveAndDeleteMonthEndProcessData(managementProcessTracker.getManagementProcessId()))
                .then(saveAndDeleteOfficeEventTracker(officeEventTracker))
                .then(saveAndDeleteSavingsAccountInterestDeposit(managementProcessTracker.getManagementProcessId()))
                .then(Mono.just("Month End Process Steps Reverted"));
    }



    private Mono<Boolean> updateSavingsAccountBalanceFromPassBookDepositAmount(String managementProcessTrackerId) {
        return getPassBookDataByTransactionCodeAndManagementProcessId(managementProcessTrackerId)
                .flatMap(passbookResponseDTOS -> {
                    List<Mono<Boolean>> updateOperations = passbookResponseDTOS
                            .stream()
                            .map(passbookResponseDTO ->
                                    getSavingAccountsBySavingsAccountIdFromPassBook(passbookResponseDTO.getSavingsAccountId())
                                            .flatMap(savingsAccountResponseDTO -> calculateAndUpdateSavingsAccountBalanceFromPassBookDepositAmount(savingsAccountResponseDTO, passbookResponseDTO)))
                            .collect(Collectors.toList());
                    return Flux.merge(updateOperations).all(result -> result).single();
                });
    }

    private Mono<Boolean> calculateAndUpdateSavingsAccountBalanceFromPassBookDepositAmount(SavingsAccountResponseDTO savingsAccountResponseDTO, PassbookResponseDTO passbookResponseDTO) {
        return Mono.just(savingsAccountResponseDTO.getBalance().subtract(passbookResponseDTO.getDepositAmount()))
                .flatMap(subtractedAmount -> {
                    if (subtractedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Account Balance Will be Negative or equal to Zero!"));
                    }
                    return savingsAccountUseCase.updateSavingsAccountBalance(savingsAccountResponseDTO.getSavingsAccountId(), subtractedAmount, Status.STATUS_ACTIVE.getValue())
                            .thenReturn(Boolean.TRUE);
                });
    }


    private Mono<SavingsAccountResponseDTO> getSavingAccountsBySavingsAccountIdFromPassBook(String savingsAccountId) {
        return savingsAccountUseCase
                .getSavingsAccountDetailsBySavingsAccountId(savingsAccountId);
    }

    private Mono<List<PassbookResponseDTO>> getPassBookDataByTransactionCodeAndManagementProcessId(String managementProcessTrackerId) {
        return passbookUseCase
                .getPassbookEntriesByTransactionCodeAndManagementProcessId(TransactionCodes.INTEREST_DEPOSIT.getValue(), managementProcessTrackerId)
                .doOnRequest(request -> log.info("Requesting Passbook Data for Transaction code: {} and Management Process Id: {}",TransactionCodes.INTEREST_DEPOSIT.getValue(), managementProcessTrackerId))
                .doOnSuccess(response -> log.info("Found Passbook Data for Transaction code: {} and Management Process Id: {} is: {}",TransactionCodes.INTEREST_DEPOSIT.getValue(), managementProcessTrackerId, response))
                .doOnError(throwable -> log.error("Error in Getting Passbook Data for Transaction code: {} and Management Process Id: {} is: {}",TransactionCodes.INTEREST_DEPOSIT.getValue(), managementProcessTrackerId, throwable.getMessage()))
                ;
    }

    private Mono<List<Transaction>> getTransactionDataByTransactionCodeAndManagementProcessId(String managementProcessTrackerId) {
        return transactionUseCase
                .getTransactionsByTransactionCodeAndManagementProcessId(TransactionCodes.INTEREST_DEPOSIT.getValue(), managementProcessTrackerId)
                ;
    }

    private Mono<Boolean> validateIfMonthEndProcessIsRevertibleForOffice(OfficeEventTracker officeEventTracker, ManagementProcessTracker managementProcessTracker) {
        return !officeEventTracker.getOfficeEvent().equalsIgnoreCase(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue()) ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process Is Not Completed for Office")) : Mono.just(Boolean.TRUE);

    }

    private Mono<OfficeEventTracker> getLastOfficeEventForOffice(String managementProcessId, String officeId) {
        return officeEventUseCase
                .getLastOfficeEventForOffice(managementProcessId, officeId)
                .doOnSuccess(response -> log.info("Found Last Office Event for Office: {} is: {}", officeId, response))
                ;
    }

    private Mono<ManagementProcessTracker> getLastManagementProcessForOffice(String officeId) {
        return managementProcessUseCase
                .getLastManagementProcessForOffice(officeId)
                .doOnRequest(request -> log.info("Requesting Last Management Process for Office: {}", officeId))
                .doOnSuccess(response -> log.info("Found Last Management Process for Office: {} is: {}", officeId, response))
                .doOnError(throwable -> log.error("Error in Getting Last Management Process for Office: {} is: {}", officeId, throwable.getMessage()))
                ;
    }

    private Mono<String> saveAndDeletePassBookData(String managementProcessTrackerId) {
        return getPassBookDataByTransactionCodeAndManagementProcessId(managementProcessTrackerId)
                .flatMap(passbookResponseDTOS ->
                        monthEndProcessDataArchivePort
                                .saveIntoPassBookHistory(passbookResponseDTOS)
                                .then(
                                        monthEndProcessDataArchivePort
                                                .deleteFromPassBook(managementProcessTrackerId)
                                )
                );
    }

    private Mono<String> saveAndDeleteTransactionData(String managementProcessTrackerId) {
        return getTransactionDataByTransactionCodeAndManagementProcessId(managementProcessTrackerId)
                .flatMap(transactions ->
                        monthEndProcessDataArchivePort
                                .saveIntoTransactionHistory(transactions)
                                .then(
                                        monthEndProcessDataArchivePort
                                                .deleteFromTransaction(managementProcessTrackerId)
                                )
                );
    }

    private Mono<String> saveAndDeleteMonthEndProcessTracker(String managementProcessTrackerId) {
        return port.getMonthEndProcessTrackerForManagementProcessId(managementProcessTrackerId)
                .flatMapMany(Flux::fromIterable)
                .flatMap(monthEndProcessTracker -> {
                    JournalRequestDTO journalRequestDTO = gson.fromJson(monthEndProcessTracker.getAisRequest(), JournalRequestDTO.class);
                    return Mono.just(this.reverseJournalRequestDTO(journalRequestDTO))
                            .filter(reversedJournalDTO -> reversedJournalDTO.getAmount().compareTo(BigDecimal.ZERO) > 0)
                            .flatMap(reversedJournalDTO -> accountingUseCase.saveAccountingJournal(reversedJournalDTO)
                                    .doOnSuccess(response -> log.info("Accounting Journal Reverted Successfully for Month End Process with transaction code: {}", monthEndProcessTracker.getTransactionCode()))
                            .thenReturn(monthEndProcessTracker));
                })
                .doOnError(throwable -> log.error("Error in Reverting Accounting Journal for Month End Process: {}", throwable.getMessage()))
                .collectList()
                .flatMap(monthEndProcessTrackers -> monthEndProcessDataArchivePort.deleteFromMonthEndProcessTracker(managementProcessTrackerId));
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

    private Mono<String> saveAndDeleteMonthEndProcessData(String managementProcessTrackerId) {
        return port.getMonthEndProcessDataForManagementProcessId(managementProcessTrackerId)
                .flatMap(monthEndProcessData ->
//                        monthEndProcessDataArchivePort
//                                .saveIntoMonthEndProcessDataHistory(monthEndProcessData)
//                                .then(
                                        monthEndProcessDataArchivePort
                                                .deleteFromMonthEndProcessData(managementProcessTrackerId)
//                                )
                );
    }

    private Mono<String> saveAndDeleteOfficeEventTracker(OfficeEventTracker officeEventTracker) {
        return monthEndProcessDataArchivePort
                .saveIntoOfficeEventTrackerHistory(officeEventTracker)
                .then(
                        monthEndProcessDataArchivePort
                                .deleteFromOfficeEventTracker(officeEventTracker)
                );
    }

    private Mono<String> saveAndDeleteSavingsAccountInterestDeposit(String managementProcessTrackerId) {
        return savingsInterestUseCase
                .getAllSavingsAccountInterestDepositsForManagementProcessId(managementProcessTrackerId)
                .flatMap(savingsAccountInterestDeposit ->
                        monthEndProcessDataArchivePort
                                .saveIntoSavingsAccountInterestDepositHistory(savingsAccountInterestDeposit)
                                .then(
                                        monthEndProcessDataArchivePort
                                                .deleteFromSavingsAccountInterestDeposit(managementProcessTrackerId)
                                )
                );

    }


    private Mono<Void> retryAndGetAccountingAndUpdateMonthEndProcessTrackerForOffice(String managementProcessId, String officeId, String loginId) {
        AtomicReference<String> monthEndProcessId = new AtomicReference<>();
        return port.getMonthEndProcessIdForOffice(managementProcessId, officeId)
                .doOnNext(monthEndProcessId::set)
                .flatMap(monthEndProcessTrackerId -> port.getMonthEndProcessTrackerEntriesByManagementProcessForOffice(managementProcessId, officeId)
                        .filter(monthEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(monthEndProcessTracker.getTransactionCode()))
                        .map(MonthEndProcessTracker::getTransactionCode)
                        .collectList())
                .flatMapIterable(transactionCodeList -> transactionCodeList)
                .flatMap(transactionCode -> port.updateMonthEndProcessTrackerStatusForRetry(managementProcessId, officeId, transactionCode, Status.STATUS_WAITING.getValue(), loginId))
                .collectList()
                .flatMap(this::getAisRequestAndResponseForMonthEndProcessList)
                .flatMap(monthEndProcessTrackerList -> {
                    if (monthEndProcessTrackerList.stream().allMatch(monthEndProcessTracker -> monthEndProcessTracker.getStatus().equals(Status.STATUS_FINISHED.getValue()))) {
                        return officeEventUseCase.insertOfficeEvent(managementProcessId, officeId, OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue(), loginId, monthEndProcessId.get())
                                .map(officeEventTracker -> monthEndProcessTrackerList);
                    }
                    return Mono.just(monthEndProcessTrackerList);
                })
                .doOnSuccess(response -> log.info("Accounting Retry by Office of Month End Process is Successful: {}", response))
                .doOnError(throwable -> log.error("Accounting Retry by Office of Month End Process is Failed: {}", throwable.getMessage()))
                /*.subscribeOn(Schedulers.immediate())
                .subscribe();*/
                .then();
    }

    private Mono<ManagementProcessTracker> checkIfAccountingProcessIsRetryableForOffice(ManagementProcessTracker managementProcessTracker) {
        return this.validateIfMonthEndProcessIsRunnableTodayForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), managementProcessTracker.getBusinessDate())
                .flatMap(aBoolean -> port.getMonthEndProcessTrackerEntriesByManagementProcessForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .filter(monthEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(monthEndProcessTracker.getTransactionCode()))
                        .filter(monthEndProcessTracker -> monthEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()))
                        .collectList())
                .filter(monthEndProcessTrackerList -> !monthEndProcessTrackerList.isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process Accounting is Not Retryable for Office")))
                .map(response -> managementProcessTracker);
    }

    private Mono<Void> retryAndGetAccountingAndUpdateMonthEndProcessTrackerByTransactionCodeForOffice(String managementProcessId, String officeId, List<String> transactionCodeList, String loginId) {
        AtomicReference<String> monthEndProcessId = new AtomicReference<>();
        return port.getMonthEndProcessIdForOffice(managementProcessId, officeId)
                .doOnNext(monthEndProcessId::set)
                .flatMapIterable(id -> transactionCodeList)
                .flatMap(transactionCode -> port.updateMonthEndProcessTrackerStatusForRetry(managementProcessId, officeId, transactionCode, Status.STATUS_WAITING.getValue(), loginId))
                .collectList()
                .flatMap(this::getAisRequestAndResponseForMonthEndProcessList)
                .flatMap(monthEndProcessTrackerList -> {
                    if (monthEndProcessTrackerList.stream().allMatch(monthEndProcessTracker -> monthEndProcessTracker.getStatus().equals(Status.STATUS_FINISHED.getValue()))) {
                        return officeEventUseCase.insertOfficeEvent(managementProcessId, officeId, OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue(), loginId, monthEndProcessId.get())
                                .map(officeEventTracker -> monthEndProcessTrackerList);
                    }
                    return Mono.just(monthEndProcessTrackerList);
                })
                .doOnSuccess(response -> log.info("Accounting Retry by Transaction Code List of Month End Process is Successful: {}", response))
                .doOnError(throwable -> log.error("Accounting Retry by Transaction Code List of Month End Process is Failed: {}", throwable.getMessage()))
                /*.subscribeOn(Schedulers.immediate())
                .subscribe();*/
                .then();
    }

    private Mono<ManagementProcessTracker> checkIfAccountingProcessIsRetryableByTransactionCodeListForOffice(ManagementProcessTracker managementProcessTracker, List<String> transactionCodeList) {
        return this.validateIfMonthEndProcessIsRunnableTodayForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), managementProcessTracker.getBusinessDate())
                .flatMap(aBoolean -> port.getMonthEndProcessTrackerEntriesByManagementProcessForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .filter(monthEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(monthEndProcessTracker.getTransactionCode()) && transactionCodeList.contains(monthEndProcessTracker.getTransactionCode()))
                        .filter(monthEndProcessTracker -> monthEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()))
                        .collectList())
                .filter(monthEndProcessTrackerList -> !monthEndProcessTrackerList.isEmpty() && monthEndProcessTrackerList.size() == transactionCodeList.size())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process Accounting Retry by Transaction Code List is Not Retryable for Office")))
                .map(response -> managementProcessTracker);
    }

    private Mono<ManagementProcessTracker> checkIfAccountingOfMonthEndProcessIsValid(ManagementProcessTracker managementProcessTracker) {
        return this.validateIfMonthEndProcessIsRunnableTodayForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), managementProcessTracker.getBusinessDate())
                .flatMap(aBoolean -> port.getMonthEndProcessTrackerEntriesByManagementProcessForOffice(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId())
                        .filter(monthEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(monthEndProcessTracker.getTransactionCode()))
                        .collectList())
                .filter(List::isEmpty)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process Accounting is Already Processing for Office")))
                .map(data -> managementProcessTracker);
    }

    private Mono<Void> getAccountingAndUpdateMonthEndProcessTrackerForOffice(ManagementProcessTracker managementProcessTracker, MonthEndProcessAccountingViewResponseDTO responseDTO, String loginId) {
        AtomicReference<String> monthEndProcessId = new AtomicReference<>();
        return port.getMonthEndProcessIdForOffice(managementProcessTracker.getManagementProcessId(), responseDTO.getOfficeId())
                .doOnNext(monthEndProcessId::set)
                .flatMap(monthEndProcessTrackerId -> this.insertMonthEndProcessTrackerEntriesForOffice(managementProcessTracker, monthEndProcessTrackerId, responseDTO, loginId))
                .flatMap(this::getAisRequestAndResponseForMonthEndProcessList)
                .flatMap(monthEndProcessTrackerList -> {
                    if (monthEndProcessTrackerList.stream().allMatch(monthEndProcessTracker -> monthEndProcessTracker.getStatus().equals(Status.STATUS_FINISHED.getValue()))) {
                        return officeEventUseCase.insertOfficeEvent(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue(), loginId, monthEndProcessId.get())
                                .map(officeEventTracker -> monthEndProcessTrackerList);
                    }
                    return Mono.just(monthEndProcessTrackerList);
                })
                .doOnSuccess(response -> log.info("Accounting of Month End Process is Successful: {}", response))
                .doOnError(throwable -> log.error("Accounting of Month End Process is Failed: {}", throwable.getMessage()))
                /*.subscribeOn(Schedulers.immediate())
                .subscribe();*/
                .then();
    }

    private Mono<List<MonthEndProcessTracker>> insertMonthEndProcessTrackerEntriesForOffice(ManagementProcessTracker managementProcessTracker, String monthEndProcessTrackerId, MonthEndProcessAccountingViewResponseDTO responseDTO, String loginId) {
        //        Dummy Data Starts
        List<MonthEndProcessProductTransaction> accruedTransactions = responseDTO.getInterestAccrued().getData().stream()
                .map(interestProductResponse -> gson.fromJson(interestProductResponse.toString(), MonthEndProcessProductTransaction.class))
                .toList();
        List<MonthEndProcessProductTransaction> postingTransactions = responseDTO.getInterestPosting().getData().stream()
                .map(interestProductResponse -> gson.fromJson(interestProductResponse.toString(), MonthEndProcessProductTransaction.class))
                .toList();
//        List<MonthEndProcessProductTransaction> accruedTransactions = new ArrayList<>();
//        List<MonthEndProcessProductTransaction> postingTransactions = new ArrayList<>();
        List<MonthEndProcessTracker> monthEndProcessTrackerList = List.of(
                this.buildMonthEndProcessTrackerEntry(managementProcessTracker.getManagementProcessId(), monthEndProcessTrackerId, responseDTO.getOfficeId(), managementProcessTracker.getBusinessDate(), TransactionCodes.INTEREST_ACCRUED.getValue(), accruedTransactions, loginId),
                this.buildMonthEndProcessTrackerEntry(managementProcessTracker.getManagementProcessId(), monthEndProcessTrackerId, responseDTO.getOfficeId(), managementProcessTracker.getBusinessDate(), TransactionCodes.INTEREST_POSTING.getValue(), postingTransactions, loginId)
        );
        log.info("Tracker List: {}", monthEndProcessTrackerList);
//        Dummy Data Ends
        return port.insertMonthEndProcessTrackerEntryList(monthEndProcessTrackerList);
    }

    private MonthEndProcessTracker buildMonthEndProcessTrackerEntry(String managementProcessId, String monthEndProcessTrackerId, String officeId, LocalDate businessDate, String transactionCode, List<MonthEndProcessProductTransaction> transactions, String loginId) {
        return MonthEndProcessTracker.builder()
                .managementProcessId(managementProcessId)
                .monthEndProcessTrackerId(monthEndProcessTrackerId)
                .officeId(officeId)
                .month(businessDate.getMonthValue())
                .year(businessDate.getYear())
                .monthEndDate(businessDate)
                .transactionCode(transactionCode)
                .transactions(transactions)
                .totalAmount(transactions.stream().map(MonthEndProcessProductTransaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                .status(Status.STATUS_WAITING.getValue())
                .createdBy(loginId)
                .createdOn(LocalDateTime.now())
                .build();
    }

    private Mono<List<MonthEndProcessTracker>> getAisRequestAndResponseForMonthEndProcessList(List<MonthEndProcessTracker> monthEndProcessTrackerList) {
        return Flux.fromIterable(monthEndProcessTrackerList)
//                .delayElements(Duration.ofSeconds(2))
                .flatMap(this::getAisRequestAndResponseByTransactionCodeAndUpdateMonthEndProcessTracker)
                .collectList();
    }

    private Mono<MonthEndProcessTracker> getAisRequestAndResponseByTransactionCodeAndUpdateMonthEndProcessTracker(MonthEndProcessTracker monthEndProcessTracker) {
        return port.updateMonthEndProcessTrackerStatus(monthEndProcessTracker.getManagementProcessId(), monthEndProcessTracker.getOfficeId(), monthEndProcessTracker.getTransactionCode(), Status.STATUS_PROCESSING.getValue())
                .flatMap(this::getAisRequestForAccountingOfMonthEndProcess)
                .flatMap(this::getAisResponseForAccountingOfMonthEndProcess);
//                .flatMap(this::checkIfAccountingOfMonthEndProcessIsSuccessful)
//                .flatMap(processTracker -> port.updateMonthEndProcessTrackerStatus(processTracker.getManagementProcessId(), processTracker.getOfficeId(), processTracker.getTransactionCode(), processTracker.getStatus()));
    }

    private Mono<MonthEndProcessTracker> getAisResponseForAccountingOfMonthEndProcess(MonthEndProcessTracker monthEndProcessTracker) {
        if (monthEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue())) {
            return Mono.just(monthEndProcessTracker);
        } else {
            return Mono.just(this.getJournalRequestDTOForAccountingOfMonthEndProcess(monthEndProcessTracker))
                    .doOnNext(journalRequestDTO -> log.info("Journal Request DTO: {}", journalRequestDTO))
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
                            .message("Error in Ais Response")
                            .build())
                    .flatMap(aisResponse -> {
                        monthEndProcessTracker.setAisResponse(aisResponse.toString());
                        if (aisResponse.getMessage().equals("Error in Ais Response")) {
                            return port.updateMonthEndProcessTrackerAisResponseData(monthEndProcessTracker.getManagementProcessId(), monthEndProcessTracker.getOfficeId(), monthEndProcessTracker.getTransactionCode(), monthEndProcessTracker.getAisResponse())
                                    .flatMap(monthEndProcess -> port.updateMonthEndProcessTrackerStatus(monthEndProcess.getManagementProcessId(), monthEndProcess.getOfficeId(), monthEndProcess.getTransactionCode(), Status.STATUS_FAILED.getValue()));
                        }
                        return port.updateMonthEndProcessTrackerAisResponseData(monthEndProcessTracker.getManagementProcessId(), monthEndProcessTracker.getOfficeId(), monthEndProcessTracker.getTransactionCode(), monthEndProcessTracker.getAisResponse())
                                .flatMap(monthEndProcess -> port.updateMonthEndProcessTrackerStatus(monthEndProcess.getManagementProcessId(), monthEndProcess.getOfficeId(), monthEndProcess.getTransactionCode(), Status.STATUS_FINISHED.getValue()));
                    });
        }
    }

    private Mono<MonthEndProcessTracker> getAisRequestForAccountingOfMonthEndProcess(MonthEndProcessTracker monthEndProcessTracker) {
        String processId = monthEndProcessTracker.getManagementProcessId().concat("_").concat(monthEndProcessTracker.getTransactionCode());
        return this.buildAisRequestForAccountingOfMonthEndProcess(monthEndProcessTracker)
//                .delayElement(Duration.ofSeconds(2))
                .flatMap(accountingUseCase::getAccountingJournalRequestBody)
                .doOnNext(journalRequestDTO -> log.info("Ais Request: {}", journalRequestDTO))
                .doOnError(throwable -> log.error("Error in Ais Request: {}", throwable.getMessage()))
                .onErrorReturn(JournalRequestDTO.builder()
                        .description("Error in Ais Request")
                        .build())
                .flatMap(journalRequestDTO -> {
                    monthEndProcessTracker.setAisRequest(journalRequestDTO.toString());
                    if (journalRequestDTO.getDescription().equals("Error in Ais Request")) {
                        return port.updateMonthEndProcessTrackerAisRequestData(monthEndProcessTracker.getManagementProcessId(), monthEndProcessTracker.getOfficeId(), monthEndProcessTracker.getTransactionCode(), monthEndProcessTracker.getAisRequest())
                                .flatMap(monthEndProcess -> port.updateMonthEndProcessTrackerStatus(monthEndProcess.getManagementProcessId(), monthEndProcess.getOfficeId(), monthEndProcess.getTransactionCode(), Status.STATUS_FAILED.getValue()));
                    }
                    journalRequestDTO.setProcessId(processId);
                    return port.updateMonthEndProcessTrackerAisRequestData(monthEndProcessTracker.getManagementProcessId(), monthEndProcessTracker.getOfficeId(), monthEndProcessTracker.getTransactionCode(), gson.toJson(journalRequestDTO));
                });
    }

    private JournalRequestDTO getJournalRequestDTOForAccountingOfMonthEndProcess(MonthEndProcessTracker monthEndProcessTracker) {
        return gson.fromJson(monthEndProcessTracker.getAisRequest(), JournalRequestDTO.class);
    }

    private Mono<AccountingRequestDTO> buildAisRequestForAccountingOfMonthEndProcess(MonthEndProcessTracker monthEndProcessTracker) {
        AtomicReference<String> processName = new AtomicReference<>();
        if (monthEndProcessTracker.getTransactionCode().equals(TransactionCodes.INTEREST_ACCRUED.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_INTEREST_ACCRUAL.getValue());
        } else if (monthEndProcessTracker.getTransactionCode().equals(TransactionCodes.INTEREST_POSTING.getValue())) {
            processName.set(AisMetaDataEnum.PROCESS_NAME_INTEREST_POSTING.getValue());
        }

        return managementProcessUseCase.getLastManagementProcessForOffice(monthEndProcessTracker.getOfficeId())
                .map(managementProcessTracker -> AccountingRequestDTO.builder()
                        .managementProcessId(monthEndProcessTracker.getManagementProcessId())
                        .mfiId(managementProcessTracker.getMfiId())
                        .officeId(monthEndProcessTracker.getOfficeId())
                        .loginId(monthEndProcessTracker.getCreatedBy())
                        .processName(processName.get())
                        .build());
    }

    private Mono<MonthEndProcessTracker> checkIfAccountingOfMonthEndProcessIsSuccessful(MonthEndProcessTracker monthEndProcessTracker) {
//        if(monthEndProcessTracker.getTransactionCode().equals(TransactionCodes.POSTING_INTEREST.getValue())){
//            monthEndProcessTracker.setStatus(Status.STATUS_FAILED.getValue());
//        } else {
//            monthEndProcessTracker.setStatus(Status.STATUS_FINISHED.getValue());
//        }
        monthEndProcessTracker.setStatus(Status.STATUS_FINISHED.getValue());
        return Mono.just(monthEndProcessTracker);
    }


    private Mono<List<MonthEndProcessTracker>> buildAndSaveMonthEndProcessTrackerEntryForOffice(String managementProcessId, String monthEndProcessTrackerId, String officeId, LocalDate businessDate, InterestAccruedResponse interestAccrued, InterestPostingResponse interestPosting, String loginId) {
//        Dummy Data Starts
        List<MonthEndProcessProductTransaction> accruedTransactions = interestAccrued.getData().stream()
                .map(interestProductResponse -> gson.fromJson(interestProductResponse.toString(), MonthEndProcessProductTransaction.class))
                .toList();
        List<MonthEndProcessProductTransaction> postingTransactions = interestPosting.getData().stream()
                .map(interestProductResponse -> gson.fromJson(interestProductResponse.toString(), MonthEndProcessProductTransaction.class))
                .toList();

        List<MonthEndProcessTracker> monthEndProcessTrackerList = List.of(
                this.buildMonthEndProcessTrackerEntry(managementProcessId, monthEndProcessTrackerId, officeId, businessDate, TransactionCodes.INTEREST_ACCRUED.getValue(), accruedTransactions, loginId),
                this.buildMonthEndProcessTrackerEntry(managementProcessId, monthEndProcessTrackerId, officeId, businessDate, TransactionCodes.INTEREST_POSTING.getValue(), postingTransactions, loginId)
        );
        log.info("Tracker List: {}", monthEndProcessTrackerList);
//        Dummy Data Ends
        return port.insertMonthEndProcessTrackerEntryList(monthEndProcessTrackerList);
    }

    private Mono<MonthEndProcessAccountingViewResponseDTO> buildMonthEndProcessAccountingGridViewResponse(String managementProcessId, MonthEndProcessAccountingViewResponseDTO responseDTO) {
        return port.getMonthEndProcessTrackerEntriesByManagementProcessForOffice(managementProcessId, responseDTO.getOfficeId())
                .filter(monthEndProcessTracker -> !HelperUtil.checkIfNullOrEmpty(monthEndProcessTracker.getTransactionCode()))
                .collectList()
                .flatMap(monthEndProcessTrackerList -> {
                    if (!monthEndProcessTrackerList.isEmpty()) {
                        monthEndProcessTrackerList.forEach(monthEndProcessTracker -> {

                            List<InterestProductResponse> data = gson.fromJson(gson.toJson(monthEndProcessTracker.getTransactions()), ArrayList.class);
                            if (monthEndProcessTracker.getTransactionCode().equals(TransactionCodes.INTEREST_ACCRUED.getValue())) {
                                InterestAccruedResponse interestAccruedResponse = InterestAccruedResponse.builder()
                                        .transactionCode(monthEndProcessTracker.getTransactionCode())
                                        .data(data)
                                        .totalAmount(monthEndProcessTracker.getTotalAmount())
                                        .status(monthEndProcessTracker.getStatus())
                                        .btnRetryEnabled(monthEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()) ? "Yes" : "No")
                                        .totalCount(data.size())
                                        .build();
                                responseDTO.setInterestAccrued(interestAccruedResponse);
                            } else if (monthEndProcessTracker.getTransactionCode().equals(TransactionCodes.INTEREST_POSTING.getValue())) {
                                InterestPostingResponse interestPostingResponse = InterestPostingResponse.builder()
                                        .transactionCode(monthEndProcessTracker.getTransactionCode())
                                        .data(data)
                                        .totalAmount(monthEndProcessTracker.getTotalAmount())
                                        .status(monthEndProcessTracker.getStatus())
                                        .btnRetryEnabled(monthEndProcessTracker.getStatus().equals(Status.STATUS_FAILED.getValue()) ? "Yes" : "No")
                                        .totalCount(data.size())
                                        .build();
                                responseDTO.setInterestPosting(interestPostingResponse);
                            }
                        });
                        return Mono.just(responseDTO);
                    }
                    return this.buildMonthEndProcessAccountingFromTransactions(managementProcessId, responseDTO);
                });
    }

    private Mono<MonthEndProcessAccountingViewResponseDTO> buildMonthEndProcessAccountingFromTransactions(String managementProcessId, MonthEndProcessAccountingViewResponseDTO responseDTO) {
        return accruedInterestUseCase.getAccruedInterestEntriesByManagementProcessIdAndOfficeId(managementProcessId, responseDTO.getOfficeId())
                .flatMap(accruedInterestResponse -> {
                    List<InterestProductResponse> interestProductResponseList = new ArrayList<>();
                    List<String> productIdList = new ArrayList<>(accruedInterestResponse.getData().stream().map(AccruedInterest::getProductId).distinct().toList());
//                    productIdList.add("SP-1001");
                    log.info("Product Id List: {}", productIdList);
                    if (productIdList.isEmpty()) {
                        return Mono.just(interestProductResponseList);
                    }
                    return commonRepository.findAllBySavingsProductIdList(productIdList)
                            .doOnNext(savingsProduct -> {
                                BigDecimal amount = accruedInterestResponse.getData().stream()
                                        .filter(accruedInterest -> accruedInterest.getProductId().equals(savingsProduct.getProductId()))
                                        .map(AccruedInterest::getAccruedInterestAmount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                                interestProductResponseList.add(InterestProductResponse.builder()
                                        .productId(savingsProduct.getProductId())
                                        .productNameEn(savingsProduct.getProductNameEn())
                                        .productNameBn(savingsProduct.getProductNameBn())
                                        .amount(amount)
                                        .build());
                            })
                            .collectList()
                            .map(savingsProductList -> interestProductResponseList);
                })
                .map(interestProductResponseList -> {
                    InterestAccruedResponse interestAccrued = InterestAccruedResponse.builder()
                            .transactionCode(TransactionCodes.INTEREST_ACCRUED.getValue())
                            .data(interestProductResponseList.stream().sorted(Comparator.comparing(InterestProductResponse::getProductId)).toList())
                            .totalAmount(interestProductResponseList.stream().map(InterestProductResponse::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                            .status(Status.STATUS_WAITING.getValue())
                            .btnRetryEnabled("No")
                            .totalCount(interestProductResponseList.size())
                            .build();
                    responseDTO.setInterestAccrued(interestAccrued);
                    return responseDTO;
                })
                .flatMap(response -> this.getInterestPostingResponseForMonthEndProcess(managementProcessId, response));
    }

    private Mono<MonthEndProcessAccountingViewResponseDTO> getInterestPostingResponseForMonthEndProcess(String managementProcessId, MonthEndProcessAccountingViewResponseDTO responseDTO) {
        return transactionUseCase
                .getAllTransactionsByManagementProcessIdAndOfficeIdAndTransactionCode(managementProcessId, responseDTO.getOfficeId(), TransactionCodes.INTEREST_DEPOSIT.getValue())
                .doOnRequest(l -> log.info("requesting transactions with managementProcessId : {} , officeId : {}, transactionCode : {}", managementProcessId, responseDTO.getOfficeId(), TransactionCodes.INTEREST_DEPOSIT.getValue()))
                .collectList()
                .doOnNext(transactions -> log.info("Interest Deposit list size : {}", transactions.size()))
                .flatMap(transactionList -> this.getSavingsProductListForTransactions(transactionList)
                        .map(accountWithProductEntities -> Tuples.of(transactionList, accountWithProductEntities)))
                .map(tuple -> {
                    Map<String, ProductWithAccountId> productAccountIdListMap = getProductAccountIdListMap(TransactionCodes.INTEREST_DEPOSIT.getValue(), tuple.getT2());

                    tuple.getT1().stream()
                            .filter(transaction -> transaction.getTransactionCode().equals(TransactionCodes.INTEREST_DEPOSIT.getValue()))
                            .forEach(transaction -> productAccountIdListMap.forEach((productId, productWithAccountId) -> {
                                if (productWithAccountId.getAccountIdList().stream().anyMatch(accountId -> (!HelperUtil.checkIfNullOrEmpty(transaction.getLoanAccountId()) && accountId.equals(transaction.getLoanAccountId())) || (!HelperUtil.checkIfNullOrEmpty(transaction.getSavingsAccountId()) && accountId.equals(transaction.getSavingsAccountId())))) {
                                    productWithAccountId.setAmount(productWithAccountId.getAmount().add(transaction.getAmount()));
                                }
                            }));

                    List<InterestProductResponse> data = new ArrayList<>();

                    productAccountIdListMap.forEach((productId, productWithAccountId) -> {
                        if (productWithAccountId.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                            data.add(InterestProductResponse.builder()
                                    .productId(productWithAccountId.getProductId())
                                    .productNameEn(productWithAccountId.getProductNameEn())
                                    .productNameBn(productWithAccountId.getProductNameBn())
                                    .amount(productWithAccountId.getAmount())
                                    .build());
                        }
                    });
                    InterestPostingResponse interestPosting = InterestPostingResponse.builder()
                            .transactionCode(TransactionCodes.INTEREST_DEPOSIT.getValue())
                            .data(data.stream().sorted(Comparator.comparing(InterestProductResponse::getProductId)).toList())
                            .totalAmount(data.stream().map(InterestProductResponse::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add))
                            .status(Status.STATUS_WAITING.getValue())
                            .btnRetryEnabled("No")
                            .totalCount(data.size())
                            .build();
                    responseDTO.setInterestPosting(interestPosting);
                    return responseDTO;
                });
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

    private Mono<List<MonthEndProcessHistory>> getMonthEndProcessHistoryListForOffice(String officeId, Integer limit, Integer offset) {
        return port.getMonthEndProcessTrackerEntriesForOffice(officeId, limit, limit * offset)
                .collectList()
                .map(this::buildMonthEndProcessHistoryFromMonthEndProcessList);
    }

    private List<MonthEndProcessHistory> buildMonthEndProcessHistoryFromMonthEndProcessList(List<MonthEndProcessTracker> monthEndProcessTrackerList) {
        List<MonthEndProcessHistory> historyList = new ArrayList<>();
        List<String> managementProcessIdList = monthEndProcessTrackerList.stream().map(MonthEndProcessTracker::getManagementProcessId).distinct().toList();
        if (!managementProcessIdList.isEmpty()) {
            managementProcessIdList.forEach(managementProcessId -> {
                List<MonthEndProcessTracker> filteredList = monthEndProcessTrackerList.stream()
                        .filter(monthEndProcessTracker -> monthEndProcessTracker.getManagementProcessId().equals(managementProcessId))
                        .toList();

                BigDecimal totalAccruedInterest = filteredList.stream()
                        .filter(monthEndProcessTracker -> monthEndProcessTracker.getTransactionCode().equals(TransactionCodes.INTEREST_ACCRUED.getValue()))
                        .findFirst()
                        .map(MonthEndProcessTracker::getTotalAmount)
                        .get();
                BigDecimal totalPostingInterest = filteredList.stream()
                        .filter(monthEndProcessTracker -> monthEndProcessTracker.getTransactionCode().equals(TransactionCodes.INTEREST_POSTING.getValue()))
                        .findFirst()
                        .map(MonthEndProcessTracker::getTotalAmount)
                        .get();

                MonthEndProcessHistory history = MonthEndProcessHistory.builder()
                        .month(Month.of(filteredList.get(0).getMonth()).name())
                        .year(filteredList.get(0).getYear())
                        .monthEndDate(filteredList.get(0).getMonthEndDate())
                        .totalAccruedAmount(totalAccruedInterest)
                        .totalPostingAmount(totalPostingInterest)
                        .status(filteredList.get(0).getStatus())
                        .build();
                historyList.add(history);
            });
            return historyList.stream().sorted(Comparator.comparing(MonthEndProcessHistory::getMonthEndDate).reversed()).toList();
        }
        return historyList;
    }

    private Mono<MonthEndProcessGridViewResponseDTO> setBtnStatusAndStatusForGridViewOfMonthEndProcess(ManagementProcessTracker managementProcessTracker, MonthEndProcessGridViewResponseDTO responseDTO) {
        return calendarUseCase.getLastWorkingDayOfAMonthOfCurrentYearForOffice(managementProcessTracker.getOfficeId(), managementProcessTracker.getBusinessDate())
                .flatMap(nextBusinessDate -> {
                    if (managementProcessTracker.getBusinessDate().equals(nextBusinessDate)) {
                        return this.getOfficeEventsAndSetStatusesForMonthEndProcessGridView(managementProcessTracker.getManagementProcessId(), managementProcessTracker.getOfficeId(), responseDTO);
                    }
                    responseDTO.setBtnRunMonthEndProcessEnabled("No");
                    responseDTO.setBtnDetailsEnabled("No");
                    responseDTO.setBtnDeleteEnabled("No");
                    responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                    responseDTO.setUserMessage("Next Month End Process Date is " + nextBusinessDate + " for this Month of Office");
                    return Mono.just(responseDTO);
                });
    }

    private Mono<MonthEndProcessGridViewResponseDTO> getOfficeEventsAndSetStatusesForMonthEndProcessGridView(String managementProcessId, String officeId, MonthEndProcessGridViewResponseDTO responseDTO) {
        return officeEventUseCase.getAllOfficeEventsForOffice(managementProcessId, officeId)
                .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                .map(OfficeEventTracker::getOfficeEvent)
                .collectList()
                .flatMap(officeEventList -> {
                    if (officeEventList.stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue()))) {
                        if (officeEventList.stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue()))) {
                            return this.getMonthEndProcessDataAndSetStatusForMonthEndProcessGridView(managementProcessId, officeId, responseDTO);
                        } else {
                            responseDTO.setBtnRunMonthEndProcessEnabled("No");
                            responseDTO.setBtnDetailsEnabled("Yes");
                            responseDTO.setBtnDeleteEnabled("Yes");
                            responseDTO.setStatus(Status.STATUS_FINISHED.getValue());
                            responseDTO.setUserMessage("Month End Process is Completed for this Month of Office");
                            return Mono.just(responseDTO);
                        }
                    }
                    responseDTO.setBtnRunMonthEndProcessEnabled("No");
                    responseDTO.setBtnDetailsEnabled("No");
                    responseDTO.setBtnDeleteEnabled("No");
                    responseDTO.setStatus(Status.STATUS_PENDING.getValue());
                    responseDTO.setUserMessage("Day End Process is Not Completed for Office. Complete Day End Process First");
                    return Mono.just(responseDTO);
                });
    }

    private Mono<MonthEndProcessGridViewResponseDTO> getMonthEndProcessDataAndSetStatusForMonthEndProcessGridView(String managementProcessId, String officeId, MonthEndProcessGridViewResponseDTO responseDTO) {
        return port.getMonthEndProcessDataEntriesForOffice(managementProcessId, officeId)
                .collectList()
                .map(monthEndProcessDataList -> {
                    if (!monthEndProcessDataList.isEmpty()) {
                        responseDTO.setBtnRunMonthEndProcessEnabled("No");
                        responseDTO.setBtnDetailsEnabled("Yes");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_PROCESSING.getValue());
                        responseDTO.setUserMessage("Month End Process is Processing for this Month of Office");
                    } else {
                        responseDTO.setBtnRunMonthEndProcessEnabled("Yes");
                        responseDTO.setBtnDetailsEnabled("No");
                        responseDTO.setBtnDeleteEnabled("No");
                        responseDTO.setStatus(Status.STATUS_WAITING.getValue());
                        responseDTO.setUserMessage("Month End Process is Not Completed for this Month of Office");
                    }
                    return responseDTO;
                });
    }
}
