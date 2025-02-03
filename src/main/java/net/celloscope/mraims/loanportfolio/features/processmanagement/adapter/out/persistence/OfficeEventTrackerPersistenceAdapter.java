package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository.OfficeEventTrackerHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository.OfficeEventTrackerRepository;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out.OfficeEventTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class OfficeEventTrackerPersistenceAdapter implements OfficeEventTrackerPersistencePort {

    private final OfficeEventTrackerRepository repository;
    private final OfficeEventTrackerHistoryRepository historyRepository;

    private final ModelMapper mapper;
    private final Gson gson;

    public OfficeEventTrackerPersistenceAdapter(OfficeEventTrackerRepository repository, OfficeEventTrackerHistoryRepository historyRepository, ModelMapper mapper) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.mapper = mapper;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<OfficeEventTracker> insertOfficeEvent(OfficeEventTracker officeEventTracker) {
        return repository.save(gson.fromJson(officeEventTracker.toString(), OfficeEventTrackerEntity.class))
                .map(entity -> gson.fromJson(entity.toString(), OfficeEventTracker.class))
                .doOnError(throwable -> log.error("Error saving office Event: {}", throwable.getMessage()));
    }

    @Override
    public Mono<OfficeEventTracker> findOfficeEventTrackerByManagementProcessIdAndOfficeIdAndOfficeEvent(String managementProcessId, String officeId, String officeEvent) {
        return repository
                .findFirstByManagementProcessIdAndOfficeIdAndOfficeEvent(managementProcessId, officeId, officeEvent)
                .map(entity -> gson.fromJson(entity.toString(), OfficeEventTracker.class))
                .doOnError(throwable -> log.error("Error finding office Event by managementProcessId - {}\nReason: {}", managementProcessId, throwable.getMessage()))
                ;
    }

    @Override
    public Mono<OfficeEventTracker> saveOfficeEventTrackerIntoHistory(OfficeEventTracker officeEventTracker) {
        return historyRepository
                .save(gson.fromJson(officeEventTracker.toString(), OfficeEventTrackerHistoryEntity.class))
                .doOnSuccess(entity -> log.info("Office Event Tracker saved into history"))
                .map(entity -> gson.fromJson(entity.toString(), OfficeEventTracker.class))
                .doOnError(throwable -> log.error("Error saving office Event into history: {}", throwable.getMessage()));
    }

    @Override
    public Mono<OfficeEventTracker> deleteOfficeEventTrackerForDayEndProcess(OfficeEventTracker officeEventTracker) {
        return repository
                .findFirstByManagementProcessIdAndOfficeIdAndOfficeEvent(officeEventTracker.getManagementProcessId(), officeEventTracker.getOfficeId(), officeEventTracker.getOfficeEvent())
                .flatMap(entity -> repository.delete(gson.fromJson(entity.toString(), OfficeEventTrackerEntity.class)))
                .doOnSuccess(entity -> log.info("Office Event Tracker Deleted SuccessFully - {}", entity))
                .doOnError(throwable -> log.error("Error deleting office Event: {}", throwable.getMessage()))
                .thenReturn(officeEventTracker);
    }

    @Override
    public Flux<OfficeEventTracker> getAllOfficeEventsForOffice(String managementProcessId, String officeId) {
        return repository.findAllByManagementProcessIdAndOfficeIdOrderByCreatedOnAsc(managementProcessId, officeId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Office Event found for management process id: " + managementProcessId)))
                .map(entity -> gson.fromJson(entity.toString(), OfficeEventTracker.class));
    }

    @Override
    public Flux<OfficeEventTracker> getAllOfficeEventsForManagementProcessId(String managementProcessId) {
        return repository
                .findAllByManagementProcessId(managementProcessId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Office Event found for management process id: " + managementProcessId)))
                .map(entity -> gson.fromJson(entity.toString(), OfficeEventTracker.class))
                ;
    }

    @Override
    public Mono<OfficeEventTracker> getLastOfficeEventForOffice(String managementProcessId, String officeId) {
        return repository
                .findFirstByManagementProcessIdAndOfficeIdOrderByCreatedOnDesc(managementProcessId, officeId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Office Event found for management process id: " + managementProcessId)))
                .map(entity -> gson.fromJson(entity.toString(), OfficeEventTracker.class));
    }

    @Override
    public Mono<String> getManagementProcessIdOfLastStagedOfficeEventForOffice(String officeId) {
        return repository
                .findFirstByOfficeIdOrderByCreatedOnDesc(officeId)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Staged Office Event found for office id: " + officeId)))
                .mapNotNull(entity -> gson.fromJson(entity.toString(), OfficeEventTracker.class))
                .filter(entity -> entity.getOfficeEvent().equalsIgnoreCase(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No Staged Office Event found for office id: " + officeId)))
                .mapNotNull(OfficeEventTracker::getManagementProcessId)
                .doOnSuccess(id -> log.info("Management Process Id Of Last Staged Office Event For Office {}: {}", officeId, id))
                .doOnError(e -> log.error("Error occurred while fetching Management Process Id Of Last Staged Office Event For Office {}: {}", officeId, e.getMessage()));
    }

    @Override
    public Mono<OfficeEventTracker> getOfficeEventTrackerByStatusForOffice(String managementProcessId, String officeId, String officeEvent) {
        return repository.findFirstByManagementProcessIdAndOfficeIdAndOfficeEvent(managementProcessId, officeId, officeEvent)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "No Office Event found for Office: " + officeId + " with Office Event: " + officeEvent)))
                .map(entity -> gson.fromJson(entity.toString(), OfficeEventTracker.class));
    }

    @Override
    public Mono<Void> deleteOfficeEventForOffice(String officeEventTrackerId, String officeId, String officeEvent) {
        return repository
                .findFirstByManagementProcessIdAndOfficeIdAndOfficeEvent(officeEventTrackerId, officeId, officeEvent)
                .doOnRequest(l -> log.info("Request received for deleting Office Event Tracker for ManagementProcessId: {}", officeEventTrackerId))
                .flatMap(repository::delete)
                .doOnSuccess(officeEventTracker -> log.info("Office Event Tracker Deleted SuccessFully"));
    }

    @Override
    public Mono<String> deleteOfficeEventTrackerForOffice(OfficeEventTracker officeEventTracker) {
        return repository
                .findFirstByManagementProcessIdAndOfficeIdAndOfficeEvent(officeEventTracker.getManagementProcessId(), officeEventTracker.getOfficeId(), officeEventTracker.getOfficeEvent())
                .doOnRequest(l -> log.info("Request received for deleting OfficeEventTracker for ManagementProcessId: {}", officeEventTracker.getManagementProcessId()))
                .flatMap(repository::delete)
                .doOnError(throwable -> log.error("Error deleting Office Event Tracker: {}", throwable.getMessage()))
                .then(Mono.just("Office Event Tracker Deleted SuccessFully"));
    }
}
