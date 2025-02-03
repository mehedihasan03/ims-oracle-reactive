package net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.enums.TransactionCodes;
import net.celloscope.mraims.loanportfolio.features.accounting.application.port.in.dto.response.JournalRequestDTO;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.repository.DayEndProcessTrackerHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.repository.DayEndProcessTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper.DayEndProcessProductTransaction;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.out.DayEndProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.domain.DayEndProcessTracker;
import net.celloscope.mraims.loanportfolio.features.transaction.domain.Transaction;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class DayEndProcessTrackerPersistenceAdapter implements DayEndProcessTrackerPersistencePort {

    private final DayEndProcessTrackerRepository repository;
    private final DayEndProcessTrackerHistoryRepository historyRepository;
    private final Gson gson;

    public DayEndProcessTrackerPersistenceAdapter(DayEndProcessTrackerRepository repository, DayEndProcessTrackerHistoryRepository historyRepository) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Flux<DayEndProcessTracker> getDayEndProcessTrackerEntriesForOffice(String managementProcessId, String officeId) {
        return repository.findAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .map(this::buildDayEndProcessTrackerDomainFromEntity);
    }

    @Override
    public Mono<List<DayEndProcessTracker>> saveDayEndProcessTrackerEntryList(List<DayEndProcessTracker> dayEndProcessTrackerList) {
        return Flux.fromIterable(dayEndProcessTrackerList)
                .map(this::buildDayEndProcessTrackerEntityFromDomain)
                .doOnNext(entity -> log.info("Day End Process tracker Entity: {}", entity))
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .map(entityList -> dayEndProcessTrackerList);
    }

    @Override
    public Mono<DayEndProcessTracker> updateDayEndProcessTrackerEntryStatus(DayEndProcessTracker dayEndProcessTracker, String status) {
        return repository.findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(dayEndProcessTracker.getManagementProcessId(), dayEndProcessTracker.getOfficeId(), dayEndProcessTracker.getTransactionCode())
                .map(entity -> {
                    entity.setStatus(status);
                    if (status.equals(Status.STATUS_PROCESSING.getValue())) {
                        entity.setProcessStartTime(LocalDateTime.now());
                    } else if (status.equals(Status.STATUS_FINISHED.getValue()) || status.equals(Status.STATUS_FAILED.getValue())) {
                        entity.setProcessEndTime(LocalDateTime.now());
                    }
                    return entity;
                })
                .flatMap(repository::save)
                .doOnNext(entity -> log.debug("Day End Process tracker Entity with Updated Status: {}", entity))
                .map(this::buildDayEndProcessTrackerDomainFromEntity);
    }

    @Override
    public Mono<String> saveAISDayEndProcessTrackerIntoHistory(DayEndProcessTracker dayEndProcessTracker) {
        return Mono.just(dayEndProcessTracker)
                .map(this::buildAISDayEndProcessTrackerHistoryEntityFromDomain)
                .flatMap(historyRepository::save)
                .doOnSuccess(s -> log.info("AIS Day End Process Tracker Entity Saved into History"))
                .thenReturn("success")
                ;
    }

    @Override
    public Mono<String> saveMISDayEndProcessTrackerIntoHistory(DayEndProcessTracker dayEndProcessTracker) {
        return Mono.just(dayEndProcessTracker)
                .map(this::buildMISDayEndProcessTrackerHistoryEntityFromDomain)
                .flatMap(historyRepository::save)
                .doOnSuccess(s -> log.info("MIS Day End Process Tracker Entity Saved into History"))
                .thenReturn("success")
                ;
    }

    @Override
    public Mono<DayEndProcessTracker> updateDayEndProcessTrackerEntryAisRequest(DayEndProcessTracker dayEndProcessTracker, JournalRequestDTO aisRequest) {
        return repository.findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(dayEndProcessTracker.getManagementProcessId(), dayEndProcessTracker.getOfficeId(), dayEndProcessTracker.getTransactionCode())
                .map(entity -> {
                    entity.setAisRequest(aisRequest.toString());
                    if (entity.getTransactionCode().equalsIgnoreCase(TransactionCodes.FEE_COLLECTION.getValue())) {
                        entity.setTotalAmount(aisRequest.getAmount());
                    }
                    return entity;
                })
                .flatMap(repository::save)
                .doOnNext(entity -> log.debug("Day End Process tracker Entity with Updated aisRequest: {}", entity))
                .map(this::buildDayEndProcessTrackerDomainFromEntity);
    }

    @Override
    public Mono<DayEndProcessTracker> updateDayEndProcessTrackerEntryAisResponse(DayEndProcessTracker dayEndProcessTracker, String aisResponse) {
        return repository.findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(dayEndProcessTracker.getManagementProcessId(), dayEndProcessTracker.getOfficeId(), dayEndProcessTracker.getTransactionCode())
                .map(entity -> {
                    entity.setAisResponse(aisResponse);
                    return entity;
                })
                .flatMap(repository::save)
                .doOnNext(entity -> log.debug("Day End Process tracker Entity with Updated aisResponse: {}", entity))
                .map(this::buildDayEndProcessTrackerDomainFromEntity);
    }

    @Override
    public Mono<List<DayEndProcessTracker>> updateDayEndProcessEntryListForRetry(List<DayEndProcessTracker> dayEndProcessTrackerList, String loginId) {
        return Flux.fromIterable(dayEndProcessTrackerList)
                .flatMap(dayEndProcessTracker -> repository.findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(dayEndProcessTracker.getManagementProcessId(), dayEndProcessTracker.getOfficeId(), dayEndProcessTracker.getTransactionCode()))
                .map(entity -> {
                    entity.setRetriedBy(loginId);
                    entity.setRetriedOn(LocalDateTime.now());
                    return entity;
                })
                .flatMap(repository::save)
                .doOnNext(entity -> log.debug("Day End Process tracker Entity with Updated aisResponse: {}", entity))
                .map(this::buildDayEndProcessTrackerDomainFromEntity)
                .collectList();
    }

    @Override
    public Mono<String> deleteDayEndProcessTrackerEntryListForOffice(String managementProcessId, String officeId) {
        return repository.findAllByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .collectList()
                .doOnNext(entityList -> log.info("Office: {}, Day End Process Entity List Size: {}, Transaction Code List: {}", officeId, entityList.size(), entityList.stream().map(DayEndProcessTrackerEntity::getTransactionCode).toList()))
                .flatMap(repository::deleteAll)
                .then(Mono.just("Day End Process Entry Deleted"));
    }

    @Override
    public Mono<String> deleteAllByManagementProcessId(String managementProcessId) {
        return repository
                .deleteAllByManagementProcessId(managementProcessId)
                .then(Mono.just("Day End Process Entry Deleted"));
    }

    @Override
    public Mono<List<DayEndProcessTrackerEntity>> getAllDayEndProcessTrackerDataByManagementProcessId(String managementProcessId) {
        return repository
                .findAllByManagementProcessId(managementProcessId)
                .collectList()
                ;
    }

    private DayEndProcessTrackerEntity buildDayEndProcessTrackerEntityFromDomain(DayEndProcessTracker dayEndProcessTracker) {
        String transactions = gson.toJson(dayEndProcessTracker.getTransactions() != null ? dayEndProcessTracker.getTransactions() : new ArrayList<>());
        dayEndProcessTracker.setTransactions(null);
        DayEndProcessTrackerEntity entity = gson.fromJson(dayEndProcessTracker.toString(), DayEndProcessTrackerEntity.class);
        entity.setTransactions(transactions);
        return entity;
    }

    private DayEndProcessTrackerHistoryEntity buildAISDayEndProcessTrackerHistoryEntityFromDomain(DayEndProcessTracker dayEndProcessTracker) {
        String transactions = gson.toJson(dayEndProcessTracker.getTransactions());
        dayEndProcessTracker.setTransactions(null);
        dayEndProcessTracker.setAisResponse(null);
        DayEndProcessTrackerHistoryEntity entity = gson.fromJson(dayEndProcessTracker.toString(), DayEndProcessTrackerHistoryEntity.class);
        entity.setTransactions(transactions);
        return entity;
    }

    private DayEndProcessTrackerHistoryEntity buildMISDayEndProcessTrackerHistoryEntityFromDomain(DayEndProcessTracker dayEndProcessTracker) {
        String transactions = gson.toJson(dayEndProcessTracker.getTransactions());
        dayEndProcessTracker.setTransactions(null);
        DayEndProcessTrackerHistoryEntity entity = gson.fromJson(dayEndProcessTracker.toString(), DayEndProcessTrackerHistoryEntity.class);
        entity.setTransactions(transactions);
        return entity;
    }

    private DayEndProcessTracker buildDayEndProcessTrackerDomainFromEntity(DayEndProcessTrackerEntity dayEndProcessTrackerEntity) {
        List<DayEndProcessProductTransaction> transactions = gson.fromJson(dayEndProcessTrackerEntity.getTransactions(), ArrayList.class);
        dayEndProcessTrackerEntity.setTransactions(null);
        DayEndProcessTracker dayEndProcessTracker = gson.fromJson(dayEndProcessTrackerEntity.toString(), DayEndProcessTracker.class);
        dayEndProcessTracker.setTransactions(transactions);
        return dayEndProcessTracker;
    }
}

