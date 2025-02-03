package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.SamityEvents;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.SamityEventTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.SamityEventTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository.SamityEventTrackerHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository.SamityEventTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out.SamityEventTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class SamityEventTrackerPersistenceAdapter implements SamityEventTrackerPersistencePort {

    private final SamityEventTrackerRepository repository;
    private final SamityEventTrackerHistoryRepository historyRepository;

    private final ModelMapper modelMapper;
    private final Gson gson;

    public SamityEventTrackerPersistenceAdapter(
            SamityEventTrackerRepository repository,
            SamityEventTrackerHistoryRepository historyRepository,
            ModelMapper modelMapper) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.modelMapper = modelMapper;
        this.gson = CommonFunctions.buildGson(this);
    }


    @Override
    public Mono<SamityEventTracker> insertSamityEvent(SamityEventTracker samityEventTracker) {
        return Mono.fromSupplier(() -> gson.fromJson(samityEventTracker.toString(), SamityEventTrackerEntity.class))
                .flatMap(repository::save)
                .map(entity -> gson.fromJson(entity.toString(), SamityEventTracker.class));
    }

    @Override
    public Flux<SamityEventTracker> getAllSamityEventsForSamity(String managementProcessId, String samityId) {
        return repository.findByManagementProcessIdAndSamityIdOrderByCreatedOnAsc(managementProcessId, samityId)
                .switchIfEmpty(Mono.just(SamityEventTrackerEntity.builder()
                        .samityId(samityId)
                        .build()))
                .map(entity -> gson.fromJson(entity.toString(), SamityEventTracker.class));
    }

    @Override
    public Mono<SamityEventTracker> getSamityEventByEventTypeForSamity(String managementProcessId, String samityId, String samityEvent) {
        return repository.findFirstByManagementProcessIdAndSamityIdAndSamityEvent(managementProcessId, samityId, samityEvent)
                .switchIfEmpty(Mono.just(SamityEventTrackerEntity.builder()
                        .managementProcessId(managementProcessId)
                        .samityId(samityId)
                        .build()))
                .map(entity -> gson.fromJson(entity.toString(), SamityEventTracker.class));
    }

    @Override
    public Mono<List<String>> deleteSamityEventTrackerByEventTrackerIdList(List<String> samityEventTrackerIdList) {
        return repository.deleteBySamityEventTrackerIdIn(samityEventTrackerIdList)
                .doOnSuccess(res -> log.info("Successfully deleted Samity Event by Tracker Id: {}", samityEventTrackerIdList.toString()))
                .doOnError(e -> log.error("An error occurred while trying to delete samity event by tracker id: {}", e.getMessage()))
                .then(Mono.just(samityEventTrackerIdList));
    }

    @Override
    public Flux<SamityEventTracker> getAllSamityEventsForOffice(String managementProcessId, String officeId) {
        return repository.findAllByManagementProcessIdAndOfficeIdOrderBySamityId(managementProcessId, officeId)
                .map(samityEventTrackerEntity -> gson.fromJson(samityEventTrackerEntity.toString(), SamityEventTracker.class));
    }

    @Override
    public Mono<List<SamityEventTracker>> insertSamityEventList(List<SamityEventTracker> samityEventTrackerList) {
        return Flux.fromIterable(samityEventTrackerList)
                .map(samityEventTracker -> gson.fromJson(samityEventTracker.toString(), SamityEventTrackerEntity.class))
                .collectList()
                .flatMapMany(repository::saveAll)
                .collectList()
                .doOnNext(entityList -> log.debug("Samity Event Entity Saved to DB: {}", entityList))
                .map(entityList -> samityEventTrackerList);
    }

    @Override
    public Mono<List<String>> deleteSamityEventTrackerByEventList(String managementProcessId, String samityId, List<String> samityEventList) {
        return Flux.fromIterable(samityEventList)
                .flatMap(samityEvent -> repository.findFirstByManagementProcessIdAndSamityIdAndSamityEvent(managementProcessId, samityId, samityEvent))
                .collectList()
                .flatMap(repository::deleteAll)
                .then(Mono.just(samityEventList))
                .doOnNext(samityEvents -> log.info("Samity: {} Events: {} are Deleted Successfully", samityId, samityEvents));
    }

    @Override
    public Mono<SamityEventTracker> getLastCollectedOrCancelledSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId) {
        return repository.findByManagementProcessIdAndSamityIdOrderByCreatedOnAsc(managementProcessId, samityId)
                .filter(entity -> Stream.of(
                                SamityEvents.CANCELED.getValue(),
                                SamityEvents.COLLECTED.getValue())
                        .anyMatch(samityEvent -> entity.getSamityEvent().equalsIgnoreCase(samityEvent)))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND,
                        "No Collected or Cancelled Samity Event found for samity: " + samityId + ", and management process: " + managementProcessId)))
                .map(entity -> gson.fromJson(entity.toString(), SamityEventTracker.class))
                .collectList()
                .filter(samityEventlist -> samityEventlist.size() == 1)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND,
                        "No Collected or Cancelled Samity Event found for samity: " + samityId + ", and management process: " + managementProcessId)))
                .mapNotNull(samityEventlist -> samityEventlist.get(0))
                .doOnSuccess(event -> log.info("Last Collected or Cancelled Samity Event For Samity {}: {}", samityId, event.toString()))
                .doOnError(e -> log.error("Error occurred while fetching Last Collected or Cancelled Samity Event For Samity {}: {}", samityId, e.getMessage()));
    }

    @Override
    public Mono<SamityEventTracker> getLastCollectedSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId) {
        return repository.findFirstByManagementProcessIdAndSamityIdAndSamityEvent(managementProcessId, samityId, SamityEvents.COLLECTED.getValue())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Collected Samity Event found for samity: " + samityId + ", and management process: " + managementProcessId)))
                .map(entity -> modelMapper.map(entity, SamityEventTracker.class))
                .doOnSuccess(event -> log.info("Last Collected Samity Event For Samity {}: {}", samityId, event.toString()))
                .doOnError(e -> log.error("Error occurred while fetching Last Collected Samity Event For Samity {}: {}", samityId, e.getMessage()));
    }

    @Override
    public Mono<SamityEventTracker> getLastAdjustedSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId) {
        return repository.findFirstByManagementProcessIdAndSamityIdAndSamityEvent(managementProcessId, samityId, SamityEvents.LOAN_ADJUSTED.getValue())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Loan Adjusted Samity Event found for samity: " + samityId + ", and management process: " + managementProcessId)))
                .map(entity -> modelMapper.map(entity, SamityEventTracker.class))
                .doOnSuccess(event -> log.info("Last Loan Adjusted Samity Event For Samity {}: {}", samityId, event.toString()))
                .doOnError(e -> log.error("Error occurred while fetching Last Loan Adjusted Samity Event For Samity {}: {}", samityId, e.getMessage()));
    }

    @Override
    public Mono<Void> deleteSamityEventTrackerByOid(String oid) {
        return repository.deleteByOid(oid)
                .doOnSuccess(res -> log.info("Successfully deleted Samity Event by Oid : {}", oid))
                .doOnError(e -> log.error("An error occurred while trying to delete samity event by Management process Id Samity Id Samity Event: {}", e.getMessage()));
    }

    @Override
    public Mono<SamityEventTracker> saveSamityEventTrackerIntoEditHistory(SamityEventTracker samityEventTracker, String loginId) {
        return Mono.just(modelMapper.map(samityEventTracker, SamityEventTrackerHistoryEntity.class))
                .mapNotNull(entity -> {
                    entity.setOid(null);
                    entity.setArchivedBy(loginId);
                    entity.setArchivedOn(LocalDateTime.now());
                    return entity;
                })
                .flatMap(historyRepository::save)
                .doOnSuccess(entity -> log.info("Samity Event Tracker saved into edit history"))
                .map(entity -> samityEventTracker)
                .doOnError(throwable -> log.error("Error saving samity Event into edit history: {}", throwable.getMessage()));
    }
}
