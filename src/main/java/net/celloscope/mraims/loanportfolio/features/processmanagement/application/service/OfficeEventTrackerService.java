package net.celloscope.mraims.loanportfolio.features.processmanagement.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out.OfficeEventTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class OfficeEventTrackerService implements OfficeEventTrackerUseCase {

    private final OfficeEventTrackerPersistencePort port;
    private final ModelMapper mapper;
    private final Gson gson;

    public OfficeEventTrackerService(OfficeEventTrackerPersistencePort port, ModelMapper mapper) {
        this.port = port;
        this.mapper = mapper;
        this.gson = CommonFunctions.buildGson(this);
    }


    @Override
    public Mono<OfficeEventTracker> insertOfficeEvent(String managementProcessId, String officeId, String officeEvent, String loginId, String officeEventTrackerId) {
        return Mono.fromSupplier(() -> OfficeEventTracker.builder()
                        .officeEventTrackerId(officeEventTrackerId)
                        .managementProcessId(managementProcessId)
                        .officeId(officeId)
                        .officeEvent(officeEvent)
                        .createdOn(LocalDateTime.now())
                        .createdBy(loginId)
                        .build())
                .doOnNext(officeEventTracker -> log.info("Office Event Tracker: {}", officeEventTracker))
                .flatMap(port::insertOfficeEvent);
    }


    @Override
    public Mono<OfficeEventTracker> updateOfficeEvent(String managementProcessId, String officeEventTrackerId, String officeId, String officeEvent, String loginId) {
//		@TODO: add explicit validation on office event tracker update
        return port.getAllOfficeEventsForOffice(managementProcessId, officeId)
                .collectList()
                .filter(list -> !list.isEmpty() && list.stream().anyMatch(item -> !HelperUtil.checkIfNullOrEmpty(item.getOfficeEvent()) && !item.getOfficeEvent().equals(officeEvent)))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "OfficeEvent \"" + officeEvent + "\" is already found")))
                .map(list -> OfficeEventTracker.builder()
                        .officeEventTrackerId(officeEventTrackerId)
                        .managementProcessId(managementProcessId)
                        .officeId(officeId)
                        .officeEvent(officeEvent)
                        .createdOn(LocalDateTime.now())
                        .createdBy(loginId)
                        .build())
                .doOnNext(officeEventTracker -> log.info("Office Event Tracker: {}", officeEventTracker))
                .flatMap(port::insertOfficeEvent);
    }

    @Override
    public Mono<OfficeEventTracker> getLastOfficeEventForOffice(String managementProcessId, String officeId) {
        return port
                .getLastOfficeEventForOffice(managementProcessId, officeId)
                .doOnNext(officeEventTracker -> log.debug("Office Event Tracker Entity: {}", officeEventTracker))
                ;
    }

    @Override
    public Mono<String> getManagementProcessIdOfLastStagedOfficeEventForOffice(String officeId) {
        return port
                .getManagementProcessIdOfLastStagedOfficeEventForOffice(officeId)
                .doOnNext(managementProcessId -> log.debug("Management Process Id of Last Staged Office Event for Office {}: {}", officeId, managementProcessId))
                .doOnSuccess(id -> log.info("Management Process Id Of Last Staged Office Event For Office {}: {}", officeId, id))
                .doOnError(e -> log.error("Error occurred while fetching Management Process Id Of Last Staged Office Event For Office {}: {}", officeId, e.getMessage()));
    }

    @Override
    public Flux<OfficeEventTracker> getAllOfficeEventsForOffice(String managementProcessId, String officeId) {
        log.debug("Office Event Tracker Update -----------------------------------: {}", managementProcessId + " " + officeId);
        return port.getAllOfficeEventsForOffice(managementProcessId, officeId)
                .doOnNext(officeEventTracker -> log.debug("Office Event Tracker Entity: {}", officeEventTracker));
    }

    @Override
    public Mono<List<OfficeEventTracker>> getAllOfficeEventsForManagementProcessId(String managementProcessId) {
        return port.getAllOfficeEventsForManagementProcessId(managementProcessId)
                .collectList()
                .doOnSuccess(officeEventTracker -> log.debug("Office Event Tracker for managementProcessId : {}", managementProcessId))
                .doOnError(throwable -> log.error("Error fetching Office Event Tracker for managementProcessId: {}", managementProcessId))
                ;
    }

    @Override
    public Mono<Void> deleteOfficeEventForOffice(String managementProcessId, String officeId, String officeEvent) {
        return port.deleteOfficeEventForOffice(managementProcessId, officeId, officeEvent);
    }

    @Override
    public Mono<String> deleteOfficeEventTracker(OfficeEventTracker officeEventTracker) {
        return port
                .deleteOfficeEventTrackerForOffice(officeEventTracker)
                .doOnRequest(l -> log.info("Request received for deleting Office Event Tracker for ManagementProcessId: {}", officeEventTracker.getManagementProcessId()))
                .doOnSuccess(officeEventTracker1 -> log.info("Office Event Tracker Deleted SuccessFully: {}", officeEventTracker1))
                .doOnError(throwable -> log.error("Error deleting Office Event Tracker: {}", throwable.getMessage()))
                ;
    }

    @Override
    public Mono<OfficeEventTracker> getOfficeEventByStatusForOffice(String managementProcessId, String officeId, String officeEvent) {
        return port.getOfficeEventTrackerByStatusForOffice(managementProcessId, officeId, officeEvent);
    }
}
