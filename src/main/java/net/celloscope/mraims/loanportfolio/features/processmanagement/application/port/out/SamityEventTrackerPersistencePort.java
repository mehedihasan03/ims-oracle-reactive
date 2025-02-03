package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out;

import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SamityEventTrackerPersistencePort {
	
	Mono<SamityEventTracker> insertSamityEvent(SamityEventTracker samityEventTracker);
	
	Flux<SamityEventTracker> getAllSamityEventsForSamity(String managementProcessId, String samityId);

	Mono<SamityEventTracker> getSamityEventByEventTypeForSamity(String managementProcessId, String samityId, String samityEvent);

    Mono<List<String>> deleteSamityEventTrackerByEventTrackerIdList(List<String> samityEventTrackerIdList);

    Flux<SamityEventTracker> getAllSamityEventsForOffice(String managementProcessId, String officeId);

    Mono<List<SamityEventTracker>> insertSamityEventList(List<SamityEventTracker> samityEventTrackerList);

    Mono<List<String>> deleteSamityEventTrackerByEventList(String managementProcessId, String samityId, List<String> samityEventList);

    Mono<SamityEventTracker> getLastCollectedOrCancelledSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId);

    Mono<SamityEventTracker> getLastCollectedSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId);

    Mono<SamityEventTracker> getLastAdjustedSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId);

    Mono<SamityEventTracker> saveSamityEventTrackerIntoEditHistory(SamityEventTracker samityEventTracker, String loginId);

    Mono<Void> deleteSamityEventTrackerByOid(String oid);
}
