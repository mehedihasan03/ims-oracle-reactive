package net.celloscope.mraims.loanportfolio.features.processmanagement.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out.SamityEventTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SamityEventTrackerService implements SamityEventTrackerUseCase {
	
	private final SamityEventTrackerPersistencePort port;
	
	private final Gson gson;
	
	public SamityEventTrackerService(SamityEventTrackerPersistencePort port) {
		this.port = port;
		this.gson = CommonFunctions.buildGson(this);
	}
	
	@Override
	public Mono<SamityEventTracker> insertSamityEvent(String managementProcessId, String samityEventTrackerId, String officeId, String samityId, String samityEvent, String loginId) {
		return port.insertSamityEvent(SamityEventTracker.builder()
						.managementProcessId(managementProcessId)
						.samityEventTrackerId(samityEventTrackerId)
						.officeId(officeId)
						.samityId(samityId)
						.samityEvent(samityEvent)
						.createdOn(LocalDateTime.now())
						.createdBy(loginId)
						.build())
				.doOnNext(samityEventTracker -> log.debug("Samity Event Saved: {}", samityEventTracker));
	}
	
	@Override
	public Mono<SamityEventTracker> updateSamityEvent(String managementProcessId, String samityId, String samityEvent, String loginId) {
		return null;
	}
	
	@Override
	public Mono<SamityEventTracker> getLastSamityEventForSamity(String managementProcessId, String samityId) {
		return null;
	}
	
	@Override
	public Flux<SamityEventTracker> getAllSamityEventsForSamity(String managementProcessId, String samityId) {
		return port.getAllSamityEventsForSamity(managementProcessId, samityId);
	}

	@Override
	public Mono<SamityEventTracker> getSamityEventByEventTypeForSamity(String managementProcessId, String samityId, String samityEvent) {
		return port.getSamityEventByEventTypeForSamity(managementProcessId, samityId, samityEvent);
	}

	@Override
	public Mono<List<String>> deleteSamityEventTrackerByEventTrackerIdList(List<String> samityEventTrackerIdList) {
		return port.deleteSamityEventTrackerByEventTrackerIdList(samityEventTrackerIdList);
	}

	@Override
	public Mono<List<SamityEventTracker>> getAllSamityEventsForOffice(String managementProcessId, String officeId) {
		return port.getAllSamityEventsForOffice(managementProcessId, officeId)
				.collectList();
	}

	@Override
	public Mono<List<SamityEventTracker>> insertSamityListForAnEvent(String managementProcessId, String officeId, List<String> samityIdList, String loginId, String samityEvent, String remarks) {
		return Flux.fromIterable(samityIdList)
				.map(samityId -> SamityEventTracker.builder()
						.managementProcessId(managementProcessId)
						.samityEventTrackerId(UUID.randomUUID().toString())
						.officeId(officeId)
						.samityId(samityId)
						.samityEvent(samityEvent)
						.remarks(remarks)
						.createdOn(LocalDateTime.now())
						.createdBy(loginId)
						.build())
				.collectList()
				.doOnNext(samityEventTrackerList -> log.info("Samity event tracker List: {}", samityEventTrackerList))
				.flatMap(port::insertSamityEventList);
	}

	@Override
	public Mono<List<String>> deleteSamityEventTrackerByEventList(String managementProcessId, String samityId, List<String> samityEventList) {
		return port.deleteSamityEventTrackerByEventList(managementProcessId, samityId, samityEventList)
				.doOnRequest(l -> log.info("Deleting Samity Event Tracker By Event List: {} {} {}",managementProcessId, samityId, samityEventList))
				.doOnSuccess(eventTrackerIds -> log.info("Deleted Samity Event Tracker By Event List: {}", eventTrackerIds));
	}

	@Override
	public Mono<SamityEventTracker> getLastCollectedOrCancelledSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId) {
		return port.getLastCollectedOrCancelledSamityEventBySamityAndManagementProcessId(samityId, managementProcessId)
				.doOnSuccess(event -> log.info("Last Collected or Cancelled Samity Event For Samity {}: {}", samityId, event.toString()))
				.doOnError(e -> log.error("Error occurred while fetching Last Collected or Cancelled Samity Event For Samity {}: {}", samityId, e.getMessage()));
	}

	@Override
	public Mono<SamityEventTracker> getLastCollectedSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId) {
		return port.getLastCollectedSamityEventBySamityAndManagementProcessId(samityId, managementProcessId)
				.doOnSuccess(event -> log.info("Last Collected Samity Event For Samity {}: {}", samityId, event.toString()))
				.doOnError(e -> log.error("Error occurred while fetching Last Collected Samity Event For Samity {}: {}", samityId, e.getMessage()));
	}

	@Override
	public Mono<SamityEventTracker> getLastAdjustedSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId) {
		return port.getLastAdjustedSamityEventBySamityAndManagementProcessId(samityId, managementProcessId)
				.doOnSuccess(event -> log.info("Last Loan Adjusted Samity Event For Samity {}: {}", samityId, event.toString()))
				.doOnError(e -> log.error("Error occurred while fetching Last Loan Adjusted Samity Event For Samity {}: {}", samityId, e.getMessage()));
	}

	@Override
	public Mono<SamityEventTracker> saveSamityEventTrackerIntoHistoryAndDeleteLiveData(SamityEventTracker samityEventTracker, String loginId) {
		return port.saveSamityEventTrackerIntoEditHistory(samityEventTracker, loginId)
				.mapNotNull(SamityEventTracker::getSamityEventTrackerId)
				.flux().collectList()
				.flatMap(port::deleteSamityEventTrackerByEventTrackerIdList)
				.mapNotNull(eventTrackerIds -> samityEventTracker)
				.doOnSuccess(eventTracker -> log.info("Successfully saved Samity Event Tracker Into History And Deleted Live Data"))
				.doOnError(e -> log.error("Failed to saved Samit Event Tracker Into History And Delete Live Data: {}", e.getMessage()));
	}

	@Override
	public Mono<Void> saveSamityEventTrackerIntoHistoryAndDeleteSamityEventTrackerData(SamityEventTracker samityEventTracker, String loginId) {
		return port.saveSamityEventTrackerIntoEditHistory(samityEventTracker, loginId)
				.doOnNext(eventTracker -> log.info("Successfully saved Samity Event Tracker History: {}", eventTracker))
				.map(SamityEventTracker::getOid)
				.doOnNext(eventTrackerId -> log.info("Deleting Samity Event Tracker id: {}", eventTrackerId))
				.flatMap(port::deleteSamityEventTrackerByOid)
				.doOnSuccess(eventTracker -> log.info("Successfully Deleted Samity Event Tracker Data"))
				.doOnError(e -> log.error("Failed to saved history and delete Samit Event Tracker Data: {}", e.getMessage()));
	}
}
