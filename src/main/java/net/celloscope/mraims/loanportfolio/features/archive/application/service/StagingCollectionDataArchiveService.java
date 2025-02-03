package net.celloscope.mraims.loanportfolio.features.archive.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity.*;
import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository.*;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.IDataArchiveUseCase;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.dto.DataArchiveRequestDTO;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.dto.DataArchiveResponseDTO;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.out.IDataArchivePort;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherDetailEntity;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherEntity;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository.AutoVoucherDetailPersistenceRepository;
import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository.AutoVoucherPersistenceRepository;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.in.AutoVoucherUseCase;
import net.celloscope.mraims.loanportfolio.features.autovoucher.application.port.out.AutoVoucherPersistencePort;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucher;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.repository.CollectionStagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.repository.DayEndProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity.DayForwardProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.application.port.out.DayForwardProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.repository.LoanAdjustmentRepository;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out.LoanAdjustmentPersistencePort;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentData;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.repository.LoanWaiverRepository;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out.LoanWaiverPersistencePort;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessDataEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessDataHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository.MonthEndProcessDataRepository;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository.MonthEndProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.out.MonthEndProcessDataArchivePort;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.out.MonthEndProcessPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.repository.LoanRebateEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.repository.LoanRebateRepository;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.out.LoanRebatePersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingAccountDataRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository.IStagingProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingAccountDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.withdraw.adapter.out.persistence.database.entity.WithdrawEntity;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.out.WithdrawPersistencePort;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.repository.IWithdrawStagingDataEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.repository.IWithdrawStagingDataRepository;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionEntity;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.repository.LoanWriteOffCollectionRepository;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.out.WriteOffCollectionPort;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StagingCollectionDataArchiveService implements IDataArchiveUseCase {

    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final IDataArchivePort port;
    private final CollectionStagingDataPersistencePort collectionPort;
    private final WithdrawPersistencePort withdrawPort;
    private final LoanAdjustmentPersistencePort loanAdjustmentPort;
    private final IStagingAccountDataPersistencePort stagingAccountDataPort;
    private final IStagingDataPersistencePort stagingDataPort;
    private final IStagingProcessTrackerPersistencePort stagingProcessTrackerPort;
    private final LoanRebatePersistencePort loanRebatePort;
    private final WriteOffCollectionPort writeOffCollectionPort;
    private final LoanWaiverPersistencePort loanWaiverPort;
    private final DayEndProcessTrackerPersistencePort dayEndProcessTrackerPort;
    private final DayForwardProcessTrackerPersistencePort dayForwardProcessTrackerPort;
    private final MonthEndProcessPersistencePort monthEndProcessPort;
    private final MonthEndProcessDataArchivePort monthEndProcessDataArchivePort;
    private final AutoVoucherPersistencePort autoVoucherPersistencePort;
    private final AutoVoucherUseCase autoVoucherUseCase;
    private final TransactionalOperator rxtx;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final ReactiveValueOperations<String, String> reactiveValueOps;
    private final Gson gson;
    private final Gson customGson;
    private final IWithdrawStagingDataEditHistoryRepository withdrawStagingDataEditHistoryRepository;

    private final Map<Class<?>, ReactiveCrudRepository<?, String>> restoreArchivedHistoryMappings;
    private final Map<Class<? extends ReactiveCrudRepository<?, String>>, ReactiveCrudRepository<?, String>> deleteArchivedHistoryMappings;
    private final LoanRebateEditHistoryRepository loanRebateEditHistoryRepository;
    private final IStagingDataRepository stagingDataRepository;
    private final IStagingAccountDataRepository stagingAccountDataRepository;
    private final IStagingProcessTrackerRepository stagingProcessTrackerRepository;
    private final CollectionStagingDataRepository collectionStagingDataRepository;
    private final IWithdrawStagingDataRepository withdrawStagingDataRepository;
    private final LoanAdjustmentRepository loanAdjustmentRepository;
    private final LoanRebateRepository loanRebateRepository;
    private final LoanWaiverRepository loanWaiverRepository;
    private final DayEndProcessTrackerRepository dayEndProcessTrackerRepository;
    private final MonthEndProcessTrackerRepository monthEndProcessTrackerRepository;
    private final MonthEndProcessDataRepository monthEndProcessDataRepository;
    private final AutoVoucherPersistenceRepository autoVoucherPersistenceRepository;
    private final AutoVoucherDetailPersistenceRepository autoVoucherDetailPersistenceRepository;
    private final LoanWriteOffCollectionRepository loanWriteOffCollectionRepository;

    private final IStagingDataHistoryRepositoryDelete stagingDataHistoryRepository;
    private final IStagingAccountHistoryRepositoryDelete stagingAccountHistoryRepository;
    private final IStagingProcessTrackerHistoryRepositoryDelete stagingProcessTrackerHistoryRepository;
    private final ICollectionHistoryRepositoryDelete collectionStagingHistoryRepository;
    private final IWithdrawHistoryRepositoryDelete withdrawStagingHistoryRepository;
    private final LoanAdjustmentDataHistoryRepositoryDelete loanAdjustmentDataHistoryRepository;
    private final ILoanRebateHistoryRepositoryDelete loanRebateDataHistoryRepository;
    private final ILoanWaiverHistoryRepositoryDelete loanWaiverDataHistoryRepository;
    private final IDayEndProcessTrackerHistoryRepositoryDelete dayEndProcessTrackerHistoryRepository;
    private final IMonthEndProcessTrackerHistoryRepositoryDelete monthEndProcessTrackerHistoryRepository;
    private final IMonthEndProcessDataHistoryRepositoryDelete monthEndProcessDataHistoryRepository;
    private final IAutoVoucherHistoryRepositoryDelete iAutoVoucherHistoryRepositoryDelete;
    private final IAutoVoucherDetailHistoryRepositoryDelete iAutoVoucherDetailHistoryRepositoryDelete;
    private final ILoanWriteOffHistoryRepository iLoanWriteOffHistoryRepository;
    private final ModelMapper modelMapper;


    public StagingCollectionDataArchiveService(
            MonthEndProcessDataArchivePort monthEndProcessDataArchivePort, AutoVoucherPersistencePort autoVoucherPersistencePort, AutoVoucherUseCase autoVoucherUseCase, TransactionalOperator rxtx,
            ReactiveValueOperations<String, String> reactiveValueOps,
            ManagementProcessTrackerUseCase managementProcessTrackerUseCase, IDataArchivePort port, CollectionStagingDataPersistencePort collectionPort, WithdrawPersistencePort withdrawPort, LoanAdjustmentPersistencePort loanAdjustmentPort, IStagingAccountDataPersistencePort stagingAccountDataPort, IStagingDataPersistencePort stagingDataPersistencePort, IStagingProcessTrackerPersistencePort stagingProcessTrackerPort, LoanRebatePersistencePort loanRebatePort, WriteOffCollectionPort writeOffCollectionPort, LoanWaiverPersistencePort loanWaiverPort, DayEndProcessTrackerPersistencePort dayEndProcessTrackerPort, DayForwardProcessTrackerPersistencePort dayForwardProcessTrackerPort, MonthEndProcessPersistencePort monthEndProcessPort, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, Gson customGson, IWithdrawStagingDataEditHistoryRepository withdrawStagingDataEditHistoryRepository, Map<Class<?>, ReactiveCrudRepository<?, String>> restoreArchivedHistoryMappings, Map<Class<? extends ReactiveCrudRepository<?, String>>, ReactiveCrudRepository<?, String>> deleteArchivedHistoryMappings, LoanRebateEditHistoryRepository loanRebateEditHistoryRepository, IStagingDataRepository stagingDataRepository, IStagingAccountDataRepository stagingAccountDataRepository, IStagingProcessTrackerRepository stagingProcessTrackerRepository, CollectionStagingDataRepository collectionStagingDataRepository, IWithdrawStagingDataRepository withdrawStagingDataRepository, LoanAdjustmentRepository loanAdjustmentRepository, LoanRebateRepository loanRebateRepository, LoanWaiverRepository loanWaiverRepository, DayEndProcessTrackerRepository dayEndProcessTrackerRepository, MonthEndProcessTrackerRepository monthEndProcessTrackerRepository, MonthEndProcessDataRepository monthEndProcessDataRepository, AutoVoucherPersistenceRepository autoVoucherPersistenceRepository, AutoVoucherDetailPersistenceRepository autoVoucherDetailPersistenceRepository, LoanWriteOffCollectionRepository loanWriteOffCollectionRepository, IStagingDataHistoryRepositoryDelete stagingDataHistoryRepository, IStagingAccountHistoryRepositoryDelete stagingAccountHistoryRepository, IStagingProcessTrackerHistoryRepositoryDelete stagingProcessTrackerHistoryRepository, ICollectionHistoryRepositoryDelete collectionStagingHistoryRepository, IWithdrawHistoryRepositoryDelete withdrawStagingHistoryRepository, LoanAdjustmentDataHistoryRepositoryDelete loanAdjustmentDataHistoryRepository, ILoanRebateHistoryRepositoryDelete loanRebateDataHistoryRepository, ILoanWaiverHistoryRepositoryDelete loanWaiverDataHistoryRepository, IDayEndProcessTrackerHistoryRepositoryDelete dayEndProcessTrackerHistoryRepository, IMonthEndProcessTrackerHistoryRepositoryDelete monthEndProcessTrackerHistoryRepository, IMonthEndProcessDataHistoryRepositoryDelete monthEndProcessDataHistoryRepository, IAutoVoucherHistoryRepositoryDelete autoVoucherHistoryRepository, IAutoVoucherDetailHistoryRepositoryDelete autoVoucherDetailHistoryRepository, ILoanWriteOffHistoryRepository iLoanWriteOffHistoryRepository, ModelMapper modelMapper) {
        this.monthEndProcessDataArchivePort = monthEndProcessDataArchivePort;
        this.autoVoucherPersistencePort = autoVoucherPersistencePort;
        this.autoVoucherUseCase = autoVoucherUseCase;
        this.rxtx = rxtx;
        this.reactiveValueOps = reactiveValueOps;
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.port = port;
        this.collectionPort = collectionPort;
        this.withdrawPort = withdrawPort;
        this.loanAdjustmentPort = loanAdjustmentPort;
        this.stagingAccountDataPort = stagingAccountDataPort;
        this.stagingDataPort = stagingDataPersistencePort;
        this.stagingProcessTrackerPort = stagingProcessTrackerPort;
        this.loanRebatePort = loanRebatePort;
        this.writeOffCollectionPort = writeOffCollectionPort;
        this.loanWaiverPort = loanWaiverPort;
        this.dayEndProcessTrackerPort = dayEndProcessTrackerPort;
        this.dayForwardProcessTrackerPort = dayForwardProcessTrackerPort;
        this.monthEndProcessPort = monthEndProcessPort;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.customGson = CommonFunctions.buildGsonExcludingProperties(customGson);
        this.withdrawStagingDataEditHistoryRepository = withdrawStagingDataEditHistoryRepository;
        this.restoreArchivedHistoryMappings = restoreArchivedHistoryMappings;
        this.deleteArchivedHistoryMappings = deleteArchivedHistoryMappings;
        this.stagingAccountDataRepository = stagingAccountDataRepository;
        this.stagingProcessTrackerRepository = stagingProcessTrackerRepository;
        this.collectionStagingDataRepository = collectionStagingDataRepository;
        this.withdrawStagingDataRepository = withdrawStagingDataRepository;
        this.loanAdjustmentRepository = loanAdjustmentRepository;
        this.loanRebateRepository = loanRebateRepository;
        this.loanWaiverRepository = loanWaiverRepository;
        this.dayEndProcessTrackerRepository = dayEndProcessTrackerRepository;
        this.monthEndProcessTrackerRepository = monthEndProcessTrackerRepository;
        this.monthEndProcessDataRepository = monthEndProcessDataRepository;
        this.autoVoucherPersistenceRepository = autoVoucherPersistenceRepository;
        this.autoVoucherDetailPersistenceRepository = autoVoucherDetailPersistenceRepository;
        this.loanWriteOffCollectionRepository = loanWriteOffCollectionRepository;
        this.stagingDataHistoryRepository = stagingDataHistoryRepository;
        this.stagingAccountHistoryRepository = stagingAccountHistoryRepository;
        this.stagingProcessTrackerHistoryRepository = stagingProcessTrackerHistoryRepository;
        this.collectionStagingHistoryRepository = collectionStagingHistoryRepository;
        this.withdrawStagingHistoryRepository = withdrawStagingHistoryRepository;
        this.loanAdjustmentDataHistoryRepository = loanAdjustmentDataHistoryRepository;
        this.loanRebateDataHistoryRepository = loanRebateDataHistoryRepository;
        this.loanWaiverDataHistoryRepository = loanWaiverDataHistoryRepository;
        this.dayEndProcessTrackerHistoryRepository = dayEndProcessTrackerHistoryRepository;
        this.monthEndProcessTrackerHistoryRepository = monthEndProcessTrackerHistoryRepository;
        this.monthEndProcessDataHistoryRepository = monthEndProcessDataHistoryRepository;
        this.iAutoVoucherHistoryRepositoryDelete = autoVoucherHistoryRepository;
        this.iAutoVoucherDetailHistoryRepositoryDelete = autoVoucherDetailHistoryRepository;
        this.iLoanWriteOffHistoryRepository = iLoanWriteOffHistoryRepository;
        this.gson = CommonFunctions.buildGson(this);
        this.loanRebateEditHistoryRepository = loanRebateEditHistoryRepository;
        this.stagingDataRepository = stagingDataRepository;
        this.modelMapper = modelMapper;
    }


    @Override
    public Mono<DataArchiveResponseDTO> archiveAndDeleteStagingDataForOffice(DataArchiveRequestDTO requestDTO) {
        return Mono.just(DataArchiveResponseDTO.builder()
                        .managementProcessId(requestDTO.getManagementProcessId())
                        .mfiId(requestDTO.getMfiId())
                        .officeId(requestDTO.getOfficeId())
                        .archivedOn(LocalDateTime.now())
                        .archivedBy(requestDTO.getLoginId())
                        .userMessage("Data Archive Process Starts For Office: " + requestDTO.getOfficeId() + " with Management Process Id: " + requestDTO.getManagementProcessId())
                        .build())
                .filter(responseDTO -> !HelperUtil.checkIfNullOrEmpty(responseDTO.getManagementProcessId()) && !HelperUtil.checkIfNullOrEmpty(responseDTO.getArchivedBy()) && !HelperUtil.checkIfNullOrEmpty(responseDTO.getOfficeId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "ManagementProcessId, loginId and officeId are required for data archiving service")))
                .doOnNext(responseDTO -> log.info("{}", responseDTO.getUserMessage()))
                .flatMap(this::archiveCollectionData)
                .flatMap(this::archiveWithdrawData)
                .flatMap(this::archiveLoanAdjustmentData)
                .flatMap(this::archiveStagingAccountData)
                .flatMap(this::archiveStagingData)
                .flatMap(this::archiveStagingProcessTrackerData)
//                .flatMap(this::archiveLoanRebateData)
//                .flatMap(this::archiveLoanWriteOffData)
//                .flatMap(this::archiveLoanWaiverData)
                .flatMap(this::archiveDayEndProcessTrackerData)
                .flatMap(this::archiveMonthEndProcessTrackerData)
                .flatMap(this::archiveMonthEndProcessData)
                .flatMap(this::archiveAutoVoucher)
//                .flatMap(this::archiveDayForwardProcessTrackerData)
                .as(this.rxtx::transactional)
                .map(responseDTO -> {
                    responseDTO.setUserMessage("Data Archive process completed Successfully");
                    return responseDTO;
                })
                .doOnError(throwable -> log.error("Failed to archive data: {}", throwable.getMessage()))
                .doOnSuccess(responseDTO -> {
                    log.info("Data Archive Response: {}", responseDTO);
                    reactiveValueOps.delete(getRedisKey(responseDTO.getOfficeId(), responseDTO.getMfiId())).subscribeOn(Schedulers.immediate())
                            .doOnSuccess(aBoolean -> log.info("Redis Key Cleared After Data Archiving Service"))
                            .subscribe();
                });
    }

    @Override
    public Mono<String> saveLoanAdjustmentIntoHistory(String managementProcessId, String processId) {
        return loanAdjustmentPort.getAllLoanAdjustmentDataByManagementProcessIdAndProcessId(managementProcessId, processId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Adjustment Data not found for history")))
                .doOnNext(list -> log.info("Loan Adjustment Data List size for moving to history: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            LoanAdjustmentDataHistoryEntity historyEntity = gson.fromJson(item.toString(), LoanAdjustmentDataHistoryEntity.class);
                            historyEntity.setOid(null);
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveLoanAdjustmentIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> "Loan adjustment data moved to history");
    }

    @Override
    public Mono<String> revertArchiveDataAndDeleteHistoryForDayForwardRoutine(String managementProcessId) {
        return this
//                .processAndDeleteByManagementProcessId(managementProcessId)
                .processAndDeleteByManagementProcessIdV2(managementProcessId)
                .flatMap(res -> loanRepaymentScheduleUseCase.revertRescheduledRepaymentSchedule(managementProcessId))
                .doOnSuccess(s -> log.info("{}", s))
                .doOnError(throwable -> log.error("Failed to restore data: {}", throwable.getMessage()))
                .thenReturn("Data restored and deleted successfully");
    }

//    public <T, ID> Mono<String> processAndDeleteByManagementProcessId(String managementProcessId) {
//        return Flux
//                .zip(
//                        Flux.fromIterable(deleteArchivedHistoryMappings.entrySet()),
//                        Flux.fromIterable(restoreArchivedHistoryMappings.entrySet())
//                )
//                .doOnRequest(l -> log.info("Data Restore Process Starts For Management Process Id: {}", managementProcessId))
//                .flatMap(tuple -> {
//                    DeleteArchiveDataBusiness<T, ID> archiveRepo = (DeleteArchiveDataBusiness<T, ID>) tuple.getT1().getValue();
//                    Class<? extends ReactiveCrudRepository<?, String>> archiveClass = tuple.getT1().getKey();
//                    RestoreArchivedDataBusiness<T, ID> restoreRepo = (RestoreArchivedDataBusiness<T, ID>) tuple.getT2().getValue();
//                    Class<T> entityType = (Class<T>) tuple.getT2().getKey();
//
//                    return archiveRepo
//                            .findAllByManagementProcessId(managementProcessId)
//                            .doOnComplete(() -> log.info("Data Restore Process Completed For Management Process Id: {}", managementProcessId))
//                            .flatMap(historyEntity -> {
//                                T formattedEntity = customGson.fromJson(historyEntity.toString(), entityType);
//                                return restoreRepo
//                                        .save(formattedEntity)
//                                        .doOnRequest(l -> log.info("Request for saving data for Process Id: {}", managementProcessId))
//                                        .doOnSuccess(s -> log.info("Data Saved Successfully into: {}", entityType.getSimpleName()))
//                                        .doOnError(throwable -> log.error("Failed to save data: {}", throwable.getMessage()))
//                                        ;
//                            })
//                            .then(archiveRepo
//                                    .deleteAllByManagementProcessId(managementProcessId)
//                                    .doOnRequest(l -> log.info("Request for deleting data for Process Id: {}", managementProcessId))
//                                    .doOnSuccess(s -> log.info("Data Deleted Successfully from: {}", archiveClass.getSimpleName()))
//                                    .doOnError(throwable -> log.error("Failed to delete data: {}", throwable.getMessage()))
//                            );
//                })
//                .then(Mono.just("Data restored and deleted successfully"));
//    }


    public <T, ID> Mono<String> processAndDeleteByManagementProcessId(String managementProcessId) {
        return Flux
                .zip(
                        Flux.fromIterable(deleteArchivedHistoryMappings.entrySet()),
                        Flux.fromIterable(restoreArchivedHistoryMappings.entrySet())
                )
                .doOnRequest(l -> log.info("Data Restore Process Starts For Management Process Id: {}", managementProcessId))
                .flatMap(tuple -> {
                    DeleteArchiveDataBusiness<T, ID> archiveRepo = (DeleteArchiveDataBusiness<T, ID>) tuple.getT1().getValue();
                    Class<? extends ReactiveCrudRepository<?, String>> archiveClass = tuple.getT1().getKey();
                    RestoreArchivedDataBusiness<T, ID> restoreRepo = (RestoreArchivedDataBusiness<T, ID>) tuple.getT2().getValue();
                    Class<T> entityType = (Class<T>) tuple.getT2().getKey();

                    return archiveRepo
                            .findAllByManagementProcessId(managementProcessId)
                            .doOnComplete(() -> log.info("Data Restore Process Completed For Management Process Id: {}", managementProcessId))
                            .flatMap(historyEntity -> {

                                // Convert historyEntity to a JSON string
                                String historyEntityJson = customGson.toJson(historyEntity);
                                log.info(">>>> String|historyEntityJson - {}", historyEntityJson);
                                log.info(">>>> Class|toString - {}", historyEntity.toString());
                                // Deserialize the JSON string to an entity of type T
                                T formattedEntity = customGson.fromJson(historyEntityJson, entityType);
                                log.info(">>>> Formatted|formattedEntity - {}", formattedEntity);


                                return restoreRepo
                                        .save(formattedEntity)
                                        .doOnRequest(l -> log.info("Request for saving data for Process Id: {}", managementProcessId))
                                        .doOnSuccess(s -> log.info("Data Saved Successfully into: {}", entityType.getSimpleName()))
                                        .doOnError(throwable -> log.error("Failed to save data: {}", throwable.getMessage()))
                                        .thenReturn(formattedEntity);  // Ensure a Mono<T> is returned to chain further
                            })
                            .flatMap(savedEntity -> {
                                return archiveRepo
                                        .deleteAllByManagementProcessId(managementProcessId)
                                        .doOnRequest(l -> log.info("Request for deleting data for Process Id: {}", managementProcessId))
                                        .doOnSuccess(s -> log.info("Data Deleted Successfully from: {}", archiveClass.getSimpleName()))
                                        .doOnError(throwable -> log.error("Failed to delete data: {}", throwable.getMessage()))
                                        .thenReturn(savedEntity);
                            });
                })
                .then(Mono.just("Data restored and deleted successfully"));
    }

    public Mono<String> processAndDeleteByManagementProcessIdV2(String managementProcessId) {
        ModelMapper mapper = new ModelMapper();
        return stagingDataHistoryRepository
                .findAllByManagementProcessId(managementProcessId)
                .map(stagingDataHistoryEntity -> {
                    String historyEntityJson = customGson.toJson(stagingDataHistoryEntity);
                    return customGson.fromJson(historyEntityJson, StagingDataEntity.class);
                })
                .collectList()
                .flatMapMany(stagingDataRepository::saveAll)
                .collectList()
                .doOnNext(s -> log.info("Staging Data Restored Successfully"))
                .flatMap(stagingDataEntities -> stagingDataHistoryRepository
                        .deleteAllByManagementProcessId(managementProcessId)
                        .doOnSuccess(s -> log.info("Staging Data Deleted Successfully"))
                        .thenReturn(stagingDataEntities))
                .flatMap(stagingDataEntities -> stagingAccountHistoryRepository
                        .findAllByManagementProcessId(managementProcessId)
                        .map(stagingAccountDataHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(stagingAccountDataHistoryEntity);
                            return customGson.fromJson(historyEntityJson, StagingAccountDataEntity.class);
                        })
                        .collectList()
                        .flatMapMany(stagingAccountDataRepository::saveAll)
                        .collectList()
                        .doOnNext(s -> log.info("Staging Account Data Restored Successfully"))
                        .flatMap(stagingAccountDataEntities -> stagingAccountHistoryRepository
                                .deleteAllByManagementProcessId(managementProcessId))
                        .doOnSuccess(s -> log.info("Staging Account Data Deleted Successfully"))
                        .thenReturn(stagingDataEntities))
                .flatMap(stagingAccountDataEntities -> stagingProcessTrackerHistoryRepository
                        .findAllByManagementProcessId(managementProcessId)
                        .map(stagingProcessTrackerHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(stagingProcessTrackerHistoryEntity);
                            return customGson.fromJson(historyEntityJson, StagingProcessTrackerEntity.class);
                        })
                        .collectList()
                        .flatMapMany(stagingProcessTrackerRepository::saveAll)
                        .collectList()
                        .doOnNext(s -> log.info("Staging Process Tracker Data Restored Successfully"))
                        .flatMap(stagingProcessTrackerEntities -> stagingProcessTrackerHistoryRepository
                                .deleteAllByManagementProcessId(managementProcessId))
                        .doOnSuccess(s -> log.info("Staging Process Tracker Data Deleted Successfully"))
                        .thenReturn(stagingAccountDataEntities))
                .flatMap(stagingProcessTrackerEntities -> collectionStagingHistoryRepository
                        .findAllByManagementProcessId(managementProcessId)
                        .map(collectionStagingDataHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(collectionStagingDataHistoryEntity);
                            return customGson.fromJson(historyEntityJson, CollectionStagingDataEntity.class);
                        })
                        .collectList()
                        .flatMapMany(collectionStagingDataRepository::saveAll)
                        .collectList()
                        .doOnNext(s -> log.info("Collection Staging Data Restored Successfully"))
                        .flatMap(collectionStagingDataEntities -> collectionStagingHistoryRepository
                                .deleteAllByManagementProcessId(managementProcessId)
                                .doOnSuccess(s -> log.info("Collection Staging Data Deleted Successfully"))
                                .thenReturn(collectionStagingDataEntities)))
                .flatMap(collectionStagingDataEntities -> withdrawStagingHistoryRepository
                        .findAllByManagementProcessId(managementProcessId)
                        .map(withdrawStagingHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(withdrawStagingHistoryEntity);
                            return customGson.fromJson(historyEntityJson, StagingWithdrawDataEntity.class);
                        })
                        .collectList()
                        .flatMapMany(withdrawStagingDataRepository::saveAll)
                        .collectList()
                        .doOnNext(s -> log.info("Withdraw Staging Data Restored Successfully"))
                        .flatMap(withdrawStagingDataEntities -> withdrawStagingHistoryRepository
                                .deleteAllByManagementProcessId(managementProcessId))
                        .doOnSuccess(s -> log.info("Withdraw Staging Data Deleted Successfully"))
                        .thenReturn(collectionStagingDataEntities))
                .flatMap(withdrawEntities -> loanAdjustmentDataHistoryRepository
                        .findAllByManagementProcessId(managementProcessId)
                        .map(loanAdjustmentHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(loanAdjustmentHistoryEntity);
                            return customGson.fromJson(historyEntityJson, LoanAdjustmentDataEntity.class);
                        })
                        .collectList()
                        .flatMapMany(loanAdjustmentRepository::saveAll)
                        .collectList()
                        .doOnNext(s -> log.info("Loan Adjustment Data Restored Successfully"))
                        .flatMap(loanAdjustmentEntities -> loanAdjustmentDataHistoryRepository
                                .deleteAllByManagementProcessId(managementProcessId))
                        .doOnSuccess(s -> log.info("Loan Adjustment Data Deleted Successfully"))
                        .thenReturn(withdrawEntities))
//                .flatMap(loanAdjustmentData -> loanRebateDataHistoryRepository
//                        .findAllByManagementProcessId(managementProcessId)
//                        .map(loanRebateHistoryEntity -> {
//                            String historyEntityJson = customGson.toJson(loanRebateHistoryEntity);
//                            return customGson.fromJson(historyEntityJson, LoanRebateEntity.class);
//                        })
//                        .collectList()
//                        .flatMapMany(loanRebateRepository::saveAll)
//                        .collectList()
//                        .doOnNext(s -> log.info("Loan Rebate Data Restored Successfully"))
//                        .flatMap(loanRebateEntities -> loanRebateDataHistoryRepository
//                                .deleteAllByManagementProcessId(managementProcessId)
//                                .doOnSuccess(s -> log.info("Loan Rebate Data Deleted Successfully"))
//                                .thenReturn(loanRebateEntities)))
                .flatMap(loanRebateEntities -> loanWaiverDataHistoryRepository
                        .findAllByManagementProcessId(managementProcessId)
                        .map(loanWaiverHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(loanWaiverHistoryEntity);
                            return customGson.fromJson(historyEntityJson, LoanWaiverEntity.class);
                        })
                        .collectList()
                        .flatMapMany(loanWaiverRepository::saveAll)
                        .collectList()
                        .doOnNext(s -> log.info("Loan Waiver Data Restored Successfully"))
                        .flatMap(loanWaiverEntities -> loanWaiverDataHistoryRepository
                                .deleteAllByManagementProcessId(managementProcessId))
                        .doOnSuccess(s -> log.info("Loan Waiver Data Deleted Successfully"))
                        .thenReturn(loanRebateEntities))
                .flatMap(loanWaiverEntities -> dayEndProcessTrackerHistoryRepository
                        .findAllByManagementProcessId(managementProcessId)
                        .map(dayEndProcessTrackerHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(dayEndProcessTrackerHistoryEntity);
                            return customGson.fromJson(historyEntityJson, DayEndProcessTrackerEntity.class);
                        })
                        .collectList()
                        .flatMapMany(dayEndProcessTrackerRepository::saveAll)
                        .collectList()
                        .doOnNext(s -> log.info("Day End Process Tracker Data Restored Successfully"))
                        .flatMap(dayEndProcessTrackerEntities -> dayEndProcessTrackerHistoryRepository
                                .deleteAllByManagementProcessId(managementProcessId)
                                .doOnSuccess(s -> log.info("Day End Process Tracker Data Deleted Successfully"))
                                .thenReturn(dayEndProcessTrackerEntities)))
                .flatMap(dayEndProcessTrackerEntities -> monthEndProcessTrackerHistoryRepository
                        .findAllByManagementProcessId(managementProcessId)
                        .map(monthEndProcessTrackerHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(monthEndProcessTrackerHistoryEntity);
                            return customGson.fromJson(historyEntityJson, MonthEndProcessTrackerEntity.class);
                        })
                        .collectList()
                        .flatMapMany(monthEndProcessTrackerRepository::saveAll)
                        .collectList()
                        .doOnNext(s -> log.info("Month End Process Tracker Data Restored Successfully"))
                        .flatMap(monthEndProcessTrackerEntities -> monthEndProcessTrackerHistoryRepository
                                .deleteAllByManagementProcessId(managementProcessId)
                                .doOnSuccess(s -> log.info("Month End Process Tracker Data Deleted Successfully"))
                                .thenReturn(monthEndProcessTrackerEntities)))
                .flatMap(monthEndProcessTrackerEntities -> monthEndProcessDataHistoryRepository
                        .findAllByManagementProcessId(managementProcessId)
                        .map(monthEndProcessDataHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(monthEndProcessDataHistoryEntity);
                            return customGson.fromJson(historyEntityJson, MonthEndProcessDataEntity.class);
                        })
                        .collectList()
                        .flatMapMany(monthEndProcessDataRepository::saveAll)
                        .collectList()
                        .doOnNext(s -> log.info("Month End Process Data Restored Successfully"))
                        .flatMap(monthEndProcessDataEntities -> monthEndProcessDataHistoryRepository
                                .deleteAllByManagementProcessId(managementProcessId)
                                .doOnSuccess(s -> log.info("Month End Process Data Deleted Successfully"))
                                .thenReturn(monthEndProcessDataEntities)))
                .flatMap(monthEndProcessDataEntities -> iAutoVoucherHistoryRepositoryDelete
                        .findAllByManagementProcessId(managementProcessId)
                        .map(autoVoucherHistoryEntity -> {
                            String historyEntityJson = customGson.toJson(autoVoucherHistoryEntity);
                            return customGson.fromJson(historyEntityJson, AutoVoucherEntity.class);
                        })
                        .collectList()
                        .flatMapMany(autoVoucherPersistenceRepository::saveAll)
                        .flatMap(autoVoucherEntity -> iAutoVoucherDetailHistoryRepositoryDelete.findAllByVoucherId(autoVoucherEntity.getVoucherId())
                                .map(autoVoucherDetailHistoryEntity -> {
                                    String historyEntityJson = customGson.toJson(autoVoucherDetailHistoryEntity);
                                    return customGson.fromJson(historyEntityJson, AutoVoucherDetailEntity.class);
                                })
                                .collectList()
                                .flatMapMany(autoVoucherDetailPersistenceRepository::saveAll)
                                .collectList()
                                .doOnNext(s -> log.info("Auto Voucher Detail Data Restored Successfully"))
                                .flatMap(autoVoucherDetailEntities -> iAutoVoucherDetailHistoryRepositoryDelete
                                        .deleteAllByVoucherId(autoVoucherEntity.getVoucherId())
                                        .doOnSuccess(s -> log.info("Auto Voucher Detail Data Deleted Successfully"))
                                        .thenReturn(autoVoucherDetailEntities))
                                .thenReturn(autoVoucherEntity))
                        .collectList()
                        .doOnNext(s -> log.info("Auto Voucher Data Restored Successfully"))
                        .flatMap(autoVoucherEntityList -> iAutoVoucherHistoryRepositoryDelete
                                .deleteAllByManagementProcessId(managementProcessId)
                        .doOnSuccess(s -> log.info("Auto Voucher Data Deleted Successfully"))
                        .thenReturn(autoVoucherEntityList)))
//                .flatMap(autoVoucherEntityList -> iLoanWriteOffHistoryRepository
//                        .findAllByManagementProcessId(managementProcessId)
//                        .map(writeOffCollectionHistoryEntity -> {
//                            String historyEntityJson = customGson.toJson(writeOffCollectionHistoryEntity);
//                            return customGson.fromJson(historyEntityJson, LoanWriteOffCollectionEntity.class);
//                        })
//                        .collectList()
//                        .flatMapMany(loanWriteOffCollectionRepository::saveAll)
//                        .collectList()
//                        .doOnNext(s -> log.info("Loan Write Off Data Restored Successfully"))
//                        .flatMap(loanWriteOffCollectionEntities -> iLoanWriteOffHistoryRepository
//                                .deleteAllByManagementProcessId(managementProcessId)
//                                .doOnSuccess(s -> log.info("Loan Write Off Data Deleted Successfully")))
//                )
                .thenReturn("Data restored and deleted successfully");

    }


    private Mono<DataArchiveResponseDTO> archiveLoanAdjustmentData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return loanAdjustmentPort.getAllLoanAdjustmentDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteLoanAdjustmentData(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteLoanAdjustmentData(List<LoanAdjustmentData> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No Loan Adjustment Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .filter(list -> list.stream().allMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getStatus()) && item.getStatus().equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Adjustment Data Verification Failed, Data Archiving Process Aborted")))
                .doOnNext(list -> log.info("Loan Adjustment Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            LoanAdjustmentDataHistoryEntity historyEntity = gson.fromJson(item.toString(), LoanAdjustmentDataHistoryEntity.class);
                            historyEntity.setOid(null);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveLoanAdjustmentIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> loanAdjustmentPort.deleteAllLoanAdjustmentDataByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }


    private Mono<DataArchiveResponseDTO> archiveCollectionData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return collectionPort.getAllCollectionDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteCollection(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> archiveWithdrawData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return withdrawPort.getAllWithdrawStagingDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteWithdraw(entityList, dataArchiveResponseDTO))
                .flatMap(dataArchiveResponseDTO1 -> withdrawStagingDataEditHistoryRepository.deleteAllByManagementProcessId(dataArchiveResponseDTO1.getManagementProcessId())
                        .thenReturn(dataArchiveResponseDTO1));
    }

    private Mono<DataArchiveResponseDTO> archiveStagingAccountData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return stagingAccountDataPort.getAllStagingAccountDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteStagingAccountData(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> archiveStagingData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return stagingDataPort.getAllStagingDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteStagingData(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> archiveStagingProcessTrackerData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return stagingProcessTrackerPort.getAllStagingProcessTrackerEntityByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteStagingProcessTrackerData(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> archiveLoanRebateData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return loanRebatePort
                .getAllLoanRebateDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteLoanRebateData(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> archiveLoanWriteOffData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return writeOffCollectionPort.getAllWrittenOffCollectionDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteLoanWriteOffData(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteLoanRebateData(List<LoanRebateEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No collection Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .filter(list -> list.stream().allMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getStatus()) && item.getStatus().equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Rebate Data Verification Failed, Data Archiving Process Aborted")))
                .doOnNext(list -> log.info("Collection Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            ModelMapper modelMapper = new ModelMapper();
                            LoanRebateHistoryEntity historyEntity = modelMapper.map(item, LoanRebateHistoryEntity.class);

                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            historyEntity.setLoanRebateDataOid(item.getOid());
                            historyEntity.setOid(null);
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveLoanRebateDataIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> loanRebatePort.deleteAllByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }


    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteLoanWriteOffData(List<LoanWriteOffCollectionEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No collection Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .filter(list -> list.stream().allMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getStatus()) && item.getStatus().equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Write Off Data Verification Failed, Data Archiving Process Aborted")))
                .doOnNext(list -> log.info("Collection Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            ModelMapper modelMapper = new ModelMapper();
                            LoanWriteOffCollectionHistoryEntity historyEntity = modelMapper.map(item, LoanWriteOffCollectionHistoryEntity.class);

                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            historyEntity.setLoanWriteOffCollectionOid(item.getOid());
                            historyEntity.setOid(null);
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveLoanWriteOffDataIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> writeOffCollectionPort.deleteAllByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }


    private Mono<DataArchiveResponseDTO> archiveLoanWaiverData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return loanWaiverPort
                .getAllLoanWaiverDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteLoanWaiverData(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteLoanWaiverData(List<LoanWaiverEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No collection Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .filter(list -> list.stream().allMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getStatus()) && item.getStatus().equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Loan Waiver Data Verification Failed, Data Archiving Process Aborted")))
                .doOnNext(list -> log.info("Collection Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
                            LoanWaiverHistoryEntity historyEntity = gson.fromJson(item.toString(), LoanWaiverHistoryEntity.class);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveLoanWaiverDataIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> loanRebatePort.deleteAllByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }

//    private Mono<DataArchiveResponseDTO> archiveLoanWriteOffData(DataArchiveResponseDTO dataArchiveResponseDTO){
//        return loanw .getAllStagingProcessTrackerEntityByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
//                .flatMap(entityList -> this.validateArchiveAndDeleteStagingProcessTrackerData(entityList, dataArchiveResponseDTO));
//    }

    private Mono<DataArchiveResponseDTO> archiveDayEndProcessTrackerData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return dayEndProcessTrackerPort
                .getAllDayEndProcessTrackerDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .flatMap(entityList -> this.validateArchiveAndDeleteDayEndProcessTrackerData(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteDayEndProcessTrackerData(List<DayEndProcessTrackerEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No collection Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
//                .filter(list -> list.stream().allMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getStatus()) && item.getStatus().equals(Status.STATUS_APPROVED.getValue())))
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Day End Process Tracker Data Verification Failed, Data Archiving Process Aborted")))
                .doOnNext(list -> log.info("Day End Process Tracker List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
                            DayEndProcessTrackerHistoryEntity historyEntity = gson.fromJson(item.toString(), DayEndProcessTrackerHistoryEntity.class);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveDayEndProcessTrackerIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> dayEndProcessTrackerPort.deleteAllByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }


    private Mono<DataArchiveResponseDTO> archiveDayForwardProcessTrackerData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return dayForwardProcessTrackerPort
                .getAllDayForwardTrackerDataByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .collectList()
                .flatMap(entityList -> this.validateArchiveAndDeleteDayForwardProcessTrackerData(entityList, dataArchiveResponseDTO));
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteDayForwardProcessTrackerData(List<DayForwardProcessTrackerEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No Day Forward Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .doOnNext(list -> log.info("Day Forward Process Tracker List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
                            DayForwardProcessTrackerHistoryEntity historyEntity = gson.fromJson(item.toString(), DayForwardProcessTrackerHistoryEntity.class);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveDayForwardProcessTrackerIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> dayForwardProcessTrackerPort.deleteAllDataByManagementProcessId(responseDTO.getManagementProcessId()).then(Mono.just("Day Forward Process Data Deleted")))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }


    private Mono<DataArchiveResponseDTO> archiveMonthEndProcessTrackerData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return monthEndProcessPort
                .getMonthEndProcessTrackerForManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .map(monthEndProcessTrackers -> monthEndProcessTrackers
                        .stream()
                        .map(value -> gson.fromJson(value.toString(), MonthEndProcessTrackerEntity.class))
                )
                .flatMap(entityList -> this.validateArchiveAndDeleteMonthEndProcessTrackerData(entityList.toList(), dataArchiveResponseDTO))
                .onErrorResume(throwable -> {
                    log.warn("Failed to archive Month End Process Tracker Data: {}", throwable.getMessage());
                    return Mono.just(dataArchiveResponseDTO);
                });
    }


    private Mono<DataArchiveResponseDTO> archiveMonthEndProcessData(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return monthEndProcessPort
                .getMonthEndProcessDataForManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .map(monthEndProcessDataList -> monthEndProcessDataList
                        .stream()
                        .map(value -> gson.fromJson(value.toString(), MonthEndProcessDataEntity.class))
                )
                .flatMap(entityList -> this.validateArchiveAndDeleteMonthEndProcessData(entityList.toList(), dataArchiveResponseDTO))
                .onErrorResume(throwable -> {
                    log.warn("Failed to archive Month End Process Data: {}", throwable.getMessage());
                    return Mono.just(dataArchiveResponseDTO);
                });
    }

    private Mono<DataArchiveResponseDTO> archiveAutoVoucher(DataArchiveResponseDTO dataArchiveResponseDTO) {
        return autoVoucherPersistencePort
                .getAutoVoucherListByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId())
                .collectList()
                .flatMap(entityList -> this.validateArchiveAndDeleteAutoVoucherData(entityList, dataArchiveResponseDTO))
                .onErrorResume(throwable -> {
                    log.error("Failed to archive Auto Voucher Data: {}", throwable.getMessage());
                    return Mono.just(dataArchiveResponseDTO);
                });
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteMonthEndProcessTrackerData(List<MonthEndProcessTrackerEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No collection Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
//                .filter(list -> list.stream().allMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getStatus()) && item.getStatus().equals(Status.STATUS_APPROVED.getValue())))
//                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Month End Process Tracker Data Verification Failed, Data Archiving Process Aborted")))
                .doOnNext(list -> log.info("Month End Process Tracker Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
                            MonthEndProcessTrackerHistoryEntity historyEntity = gson.fromJson(item.toString(), MonthEndProcessTrackerHistoryEntity.class);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveMonthEndProcessTrackerIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> monthEndProcessDataArchivePort.deleteFromMonthEndProcessTracker(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }


    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteMonthEndProcessData(List<MonthEndProcessDataEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No Month End Process Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .doOnNext(list -> log.info("Month End Process Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
                            MonthEndProcessDataHistoryEntity historyEntity = gson.fromJson(item.toString(), MonthEndProcessDataHistoryEntity.class);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveMonthEndProcessDataIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> monthEndProcessPort.deleteAllByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }


    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteAutoVoucherData(List<AutoVoucher> autoVouchers, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (autoVouchers.isEmpty()) {
            String s = "No Auto Voucher Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(autoVouchers)
                .doOnNext(list -> log.info("Auto Voucher Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .peek(item -> {
                            item.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            item.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                        })
                        .toList())
                .flatMap(autoVoucherList -> autoVoucherUseCase.saveAutoVoucherHistoryAndVoucherDetailHistoryForArchiving(autoVoucherList)
                        .then(autoVoucherUseCase.deleteAutoVoucherListByManagementProcessId(dataArchiveResponseDTO.getManagementProcessId()))
                )
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }


    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteCollection(List<CollectionStagingDataEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No collection Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .filter(list -> list.stream().allMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getStatus()) && item.getStatus().equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data Verification Failed, Data Archiving Process Aborted")))
                .doOnNext(list -> log.info("Collection Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
                            CollectionStagingDataHistoryEntity historyEntity = gson.fromJson(item.toString(), CollectionStagingDataHistoryEntity.class);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveCollectionIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> collectionPort.deleteAllCollectionDataByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteWithdraw(List<WithdrawEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No Withdraw Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .filter(list -> list.stream().allMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getStatus()) && item.getStatus().equals(Status.STATUS_APPROVED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Withdraw Data Verification Failed, Data Archiving Process Aborted")))
                .doOnNext(list -> log.info("Withdraw Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            ModelMapper modelMapper = new ModelMapper();
                            WithdrawStagingDataHistoryEntity historyEntity = modelMapper.map(item, WithdrawStagingDataHistoryEntity.class);

                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            historyEntity.setWithdrawStagingDataOid(item.getOid());
                            historyEntity.setOid(null);
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveWithdrawIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> withdrawPort.deleteAllWithdrawStagingDataByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteStagingAccountData(List<StagingAccountDataEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No Staging Account Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .doOnNext(list -> log.info("Staging Account Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
                            StagingAccountDataHistoryEntity historyEntity = gson.fromJson(item.toString(), StagingAccountDataHistoryEntity.class);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveStagingAccountDataIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> stagingAccountDataPort.deleteAllStagingAccountDataByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteStagingData(List<StagingDataEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No Staging Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .doOnNext(list -> log.info("Staging Data List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
                            StagingDataHistoryEntity historyEntity = gson.fromJson(item.toString(), StagingDataHistoryEntity.class);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveStagingDataIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> stagingDataPort.deleteAllStagingDataByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }

    private Mono<DataArchiveResponseDTO> validateArchiveAndDeleteStagingProcessTrackerData(List<StagingProcessTrackerEntity> entityList, DataArchiveResponseDTO dataArchiveResponseDTO) {
        if (entityList.isEmpty()) {
            String s = "No Staging Process Tracker Data Found to Archive";
            log.info("{}", s);
            dataArchiveResponseDTO.setUserMessage(s);
            return Mono.just(dataArchiveResponseDTO);
        }
        return Mono.just(entityList)
                .doOnNext(list -> log.info("Staging Process Tracker List size: {}", list.size()))
                .map(list -> list.stream()
                        .map(item -> {
                            item.setOid(null);
                            StagingProcessTrackerHistoryEntity historyEntity = gson.fromJson(item.toString(), StagingProcessTrackerHistoryEntity.class);
                            historyEntity.setArchivedOn(dataArchiveResponseDTO.getArchivedOn());
                            historyEntity.setArchivedBy(dataArchiveResponseDTO.getArchivedBy());
                            return historyEntity;
                        })
                        .toList())
                .flatMap(port::saveStagingProcessTrackerIntoHistory)
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO))
                .flatMap(responseDTO -> stagingProcessTrackerPort.deleteAllStagingProcessTrackerEntityByManagementProcessId(responseDTO.getManagementProcessId()))
                .doOnNext(s -> log.info("{}", s))
                .map(s -> {
                    dataArchiveResponseDTO.setUserMessage(s);
                    return dataArchiveResponseDTO;
                })
                .doOnNext(responseDTO -> log.debug("Data Archive Response: {}", responseDTO));
    }

    private String getRedisKey(String officeId, String mfiId) {
        return Constants.STAGING_DATA_GENERATION_STATUS + "-" + mfiId + "-" + officeId;
    }

}
