package net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessDataEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository.MonthEndProcessDataRepository;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository.MonthEndProcessTrackerHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository.MonthEndProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.out.MonthEndProcessPersistencePort;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessData;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessProductTransaction;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessTracker;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MonthEndProcessPersistenceAdapter implements MonthEndProcessPersistencePort {

    private final MonthEndProcessTrackerRepository trackerRepository;
    private final MonthEndProcessDataRepository dataRepository;
    private final MonthEndProcessTrackerHistoryRepository historyRepository;
    private final Gson gson;
    private final ModelMapper modelMapper;

    public MonthEndProcessPersistenceAdapter(MonthEndProcessTrackerRepository trackerRepository, MonthEndProcessDataRepository dataRepository, MonthEndProcessTrackerHistoryRepository historyRepository, ModelMapper modelMapper) {
        this.trackerRepository = trackerRepository;
        this.dataRepository = dataRepository;
        this.historyRepository = historyRepository;
        this.gson = CommonFunctions.buildGson(this);
        this.modelMapper = modelMapper;
    }

    @Override
    public Flux<MonthEndProcessTracker> getMonthEndProcessTrackerEntriesForOffice(String officeId, Integer limit, Integer offset) {
        return trackerRepository.findAllByOfficeIdOrderByMonthEndDateDesc(officeId, limit, offset)
                .map(this::buildMonthEndProcessTrackerFromEntity);
    }

    @Override
    public Flux<MonthEndProcessTracker> getMonthEndProcessTrackerEntriesByManagementProcessForOffice(String managementProcessId, String officeId) {
        return trackerRepository.findAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .switchIfEmpty(Mono.just(MonthEndProcessTrackerEntity.builder()
                        .managementProcessId(managementProcessId)
                        .officeId(officeId)
                        .build()))
                .map(this::buildMonthEndProcessTrackerFromEntity);
    }

    @Override
    public Mono<List<MonthEndProcessTracker>> insertMonthEndProcessTrackerEntryList(List<MonthEndProcessTracker> monthEndProcessTrackerList) {
        List<MonthEndProcessTrackerEntity> entityList = monthEndProcessTrackerList.stream().map(this::buildMonthEndProcessTrackerEntityFromDomain).toList();
//        log.info("Entity List: {}", entityList);
        return trackerRepository.saveAll(entityList)
                .collectList()
                .map(savedEntityList -> monthEndProcessTrackerList);
    }

    @Override
    public Mono<String> saveMonthEndProcessTrackerEntriesIntoHistory(List<MonthEndProcessTracker> monthEndProcessTrackerList) {
        return Flux
                .fromIterable(monthEndProcessTrackerList)
                .map(this::buildMonthEndProcessHistoryTrackerEntityFromDomain)
                .collectList()
                .flatMapMany(historyRepository::saveAll)
                .then(Mono.just("Successfully saved MonthEndProcessTracker into history"))
                ;
    }

    @Override
    public Mono<String> deleteFromMonthEndProcessTrackerByManagementProcessId(String managementProcessId) {
        return trackerRepository
                .findAllByManagementProcessId(managementProcessId)
                .collectList()
                .flatMap(trackerRepository::deleteAll)
                .then(Mono.just("Successfully deleted from MonthEndProcessTracker for Management Process Id: " + managementProcessId));
    }

    @Override
    public Mono<MonthEndProcessTracker> updateMonthEndProcessTrackerStatus(String managementProcessId, String officeId, String transactionCode, String status) {
        return trackerRepository.findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(managementProcessId, officeId, transactionCode)
                .doOnNext(trackerEntity -> {
                    trackerEntity.setStatus(status);
                    if (status.equals(Status.STATUS_PROCESSING.getValue())) {
                        trackerEntity.setProcessStartTime(LocalDateTime.now());
                    } else if (status.equals(Status.STATUS_FINISHED.getValue()) || status.equals(Status.STATUS_FAILED.getValue())) {
                        trackerEntity.setProcessEndTime(LocalDateTime.now());
                    } else if (status.equals(Status.STATUS_WAITING.getValue())) {
                        trackerEntity.setProcessStartTime(null);
                        trackerEntity.setProcessEndTime(null);
                    }
                })
                .flatMap(trackerRepository::save)
                .map(this::buildMonthEndProcessTrackerFromEntity);
    }

    @Override
    public Mono<MonthEndProcessTracker> updateMonthEndProcessTrackerStatusForRetry(String managementProcessId, String officeId, String transactionCode, String status, String loginId) {
        return trackerRepository.findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(managementProcessId, officeId, transactionCode)
                .doOnNext(trackerEntity -> {
                    trackerEntity.setStatus(status);
                    trackerEntity.setProcessStartTime(null);
                    trackerEntity.setProcessEndTime(null);
                    trackerEntity.setRetriedOn(LocalDateTime.now());
                    trackerEntity.setRetriedBy(loginId);
                })
                .flatMap(trackerRepository::save)
                .map(this::buildMonthEndProcessTrackerFromEntity);
    }

    @Override
    public Mono<MonthEndProcessTracker> updateMonthEndProcessTrackerAisRequestData(String managementProcessId, String officeId, String transactionCode, String aisRequest) {
        return trackerRepository.findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(managementProcessId, officeId, transactionCode)
                .doOnNext(trackerEntity -> trackerEntity.setAisRequest(aisRequest))
                .flatMap(trackerRepository::save)
                .map(this::buildMonthEndProcessTrackerFromEntity);
    }

    @Override
    public Mono<MonthEndProcessTracker> updateMonthEndProcessTrackerAisResponseData(String managementProcessId, String officeId, String transactionCode, String aisResponse) {
        return trackerRepository.findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(managementProcessId, officeId, transactionCode)
                .doOnNext(trackerEntity -> trackerEntity.setAisResponse(aisResponse))
                .flatMap(trackerRepository::save)
                .map(this::buildMonthEndProcessTrackerFromEntity);
    }

    @Override
    public Flux<MonthEndProcessData> getMonthEndProcessDataEntriesForOffice(String managementProcessId, String officeId) {
        return dataRepository.findAllByManagementProcessIdAndOfficeIdOrderBySamityId(managementProcessId, officeId)
                .map(entity -> gson.fromJson(entity.toString(), MonthEndProcessData.class));
    }

    @Override
    public Mono<List<MonthEndProcessTracker>> getMonthEndProcessTrackerForManagementProcessId(String managementProcessId) {
        return trackerRepository
                .findAllByManagementProcessId(managementProcessId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Month End Process Tracker found for Management Process Id: " + managementProcessId)))
                .map(monthEndProcessTrackerEntity -> modelMapper.map(monthEndProcessTrackerEntity, MonthEndProcessTracker.class))
                .collectList()
                ;
    }

    @Override
    public Mono<List<MonthEndProcessData>> getMonthEndProcessDataForManagementProcessId(String managementProcessId) {
        return dataRepository
                .findAllByManagementProcessId(managementProcessId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Month End Process Data found for Management Process Id: " + managementProcessId)))
                .map(monthEndProcessDataEntity -> modelMapper.map(monthEndProcessDataEntity, MonthEndProcessData.class))
                .collectList()
                ;
    }

    @Override
    public Mono<List<MonthEndProcessData>> insertMonthEndProcessDataList(List<MonthEndProcessData> monthEndProcessDataList) {
        return Flux.fromIterable(monthEndProcessDataList)
                .map(monthEndProcessData -> gson.fromJson(monthEndProcessData.toString(), MonthEndProcessDataEntity.class))
                .collectList()
                .flatMapMany(dataRepository::saveAll)
                .collectList()
                .map(entityList -> monthEndProcessDataList);
    }

    @Override
    public Mono<MonthEndProcessData> updateMonthEndProcessDataForProcessing(MonthEndProcessData monthEndProcessData) {
        return dataRepository.findFirstByManagementProcessIdAndSamityId(monthEndProcessData.getManagementProcessId(), monthEndProcessData.getSamityId())
                .doOnNext(entity -> {
                    entity.setStatus(Status.STATUS_PROCESSING.getValue());
                    entity.setProcessStartTime(LocalDateTime.now());
                })
                .flatMap(dataRepository::save)
                .map(entity -> gson.fromJson(entity.toString(), MonthEndProcessData.class));
    }

    @Override
    public Mono<MonthEndProcessData> updateMonthEndProcessDataForTotalAccruedAndPostingAmount(MonthEndProcessData monthEndProcessData, BigDecimal totalAccruedAmount, BigDecimal totalPostingAmount) {
        return dataRepository.findFirstByManagementProcessIdAndSamityId(monthEndProcessData.getManagementProcessId(), monthEndProcessData.getSamityId())
                .doOnNext(entity -> {
                    entity.setTotalAccruedAmount(totalAccruedAmount);
                    entity.setTotalPostingAmount(totalPostingAmount);
                })
                .flatMap(dataRepository::save)
                .map(entity -> gson.fromJson(entity.toString(), MonthEndProcessData.class));
    }

    @Override
    public Mono<MonthEndProcessData> updateMonthEndProcessDataForTotalPostingAmount(MonthEndProcessData monthEndProcessData, BigDecimal totalPostingAmount) {
        return dataRepository.findFirstByManagementProcessIdAndSamityId(monthEndProcessData.getManagementProcessId(), monthEndProcessData.getSamityId())
                .doOnNext(entity -> {
                    entity.setTotalAccruedAmount(totalPostingAmount);
                })
                .flatMap(dataRepository::save)
                .map(entity -> gson.fromJson(entity.toString(), MonthEndProcessData.class));
    }

    @Override
    public Mono<MonthEndProcessData> updateMonthEndProcessDataForFailed(MonthEndProcessData monthEndProcessData, String remarks) {
        return dataRepository.findFirstByManagementProcessIdAndSamityId(monthEndProcessData.getManagementProcessId(), monthEndProcessData.getSamityId())
                .doOnNext(entity -> {
                    entity.setStatus(Status.STATUS_FAILED.getValue());
                    entity.setRemarks(remarks);
                    entity.setProcessEndTime(LocalDateTime.now());
                })
                .flatMap(dataRepository::save)
                .map(entity -> gson.fromJson(entity.toString(), MonthEndProcessData.class));
    }

    @Override
    public Mono<MonthEndProcessData> updateMonthEndProcessDataForFinished(MonthEndProcessData monthEndProcessData) {
        return dataRepository.findFirstByManagementProcessIdAndSamityId(monthEndProcessData.getManagementProcessId(), monthEndProcessData.getSamityId())
                .doOnNext(entity -> {
                    entity.setStatus(Status.STATUS_FINISHED.getValue());
                    entity.setProcessEndTime(LocalDateTime.now());
                })
                .flatMap(dataRepository::save)
                .map(entity -> gson.fromJson(entity.toString(), MonthEndProcessData.class));
    }

    @Override
    public Mono<String> getMonthEndProcessIdForOffice(String managementProcessId, String officeId) {
        return dataRepository.findFirstByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .map(MonthEndProcessDataEntity::getMonthEndProcessTrackerId);
    }

    @Override
    public Mono<MonthEndProcessData> updateMonthEndProcessDataForRetry(String managementProcessId, String samityId, String loginId) {
        return dataRepository.findFirstByManagementProcessIdAndSamityId(managementProcessId, samityId)
                .map(dataEntity -> {
                    dataEntity.setStatus(Status.STATUS_WAITING.getValue());
                    dataEntity.setProcessStartTime(null);
                    dataEntity.setProcessEndTime(null);
                    dataEntity.setRetriedOn(LocalDateTime.now());
                    dataEntity.setRetriedBy(loginId);
                    return dataEntity;
                })
                .flatMap(dataRepository::save)
                .map(entity -> gson.fromJson(entity.toString(), MonthEndProcessData.class));
    }

    @Override
    public Mono<String> deleteAllByManagementProcessId(String managementProcessId) {
        return dataRepository
                .deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Month End Data Deleted Successfully"))
                ;
    }

    private MonthEndProcessTracker buildMonthEndProcessTrackerFromEntity(MonthEndProcessTrackerEntity trackerEntity) {
        List<MonthEndProcessProductTransaction> transactions = new ArrayList<>();
        if (!HelperUtil.checkIfNullOrEmpty(trackerEntity.getTransactions())) {
            transactions = gson.fromJson(trackerEntity.getTransactions(), ArrayList.class);
        }
        trackerEntity.setTransactions(null);
        MonthEndProcessTracker monthEndProcessTracker = gson.fromJson(trackerEntity.toString(), MonthEndProcessTracker.class);
        monthEndProcessTracker.setTransactions(transactions);
        return monthEndProcessTracker;
    }

    private MonthEndProcessTrackerEntity buildMonthEndProcessTrackerEntityFromDomain(MonthEndProcessTracker monthEndProcessTracker) {
        String transactions = gson.toJson(monthEndProcessTracker.getTransactions());
        monthEndProcessTracker.setTransactions(null);
        MonthEndProcessTrackerEntity trackerEntity = gson.fromJson(monthEndProcessTracker.toString(), MonthEndProcessTrackerEntity.class);
        trackerEntity.setTransactions(transactions);
        return trackerEntity;
    }

    private MonthEndProcessTrackerHistoryEntity buildMonthEndProcessHistoryTrackerEntityFromDomain(MonthEndProcessTracker monthEndProcessTracker) {
        String transactions = gson.toJson(monthEndProcessTracker.getTransactions());
        monthEndProcessTracker.setTransactions(null);
        MonthEndProcessTrackerHistoryEntity trackerEntity = gson.fromJson(monthEndProcessTracker.toString(), MonthEndProcessTrackerHistoryEntity.class);
        trackerEntity.setTransactions(transactions);
        return trackerEntity;
    }
}
