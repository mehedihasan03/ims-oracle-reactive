package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in;

import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SamityEventTrackerUseCase {
	
	Mono<SamityEventTracker> insertSamityEvent(String managementProcessId, String samityEventTrackerId, String officeId, String samityId, String samityEvent, String loginId);
	
	Mono<SamityEventTracker> updateSamityEvent(String managementProcessId, String samityId, String samityEvent, String loginId);
	
	Mono<SamityEventTracker> getLastSamityEventForSamity(String managementProcessId, String samityId);
	
	Flux<SamityEventTracker> getAllSamityEventsForSamity(String managementProcessId, String samityId);

	Mono<SamityEventTracker> getSamityEventByEventTypeForSamity(String managementProcessId, String samityId, String samityEvent);

    Mono<List<String>> deleteSamityEventTrackerByEventTrackerIdList(List<String> samityEventTrackerIdList);
	Mono<List<SamityEventTracker>> getAllSamityEventsForOffice(String managementProcessId, String officeId);

	Mono<List<SamityEventTracker>> insertSamityListForAnEvent(String managementProcessId, String officeId, List<String> samityIdList, String loginId, String samityEvent, String remarks);
	Mono<List<String>> deleteSamityEventTrackerByEventList(String managementProcessId, String samityId, List<String> samityEventList);

	Mono<SamityEventTracker> getLastCollectedOrCancelledSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId);

	Mono<SamityEventTracker> getLastCollectedSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId);

	Mono<SamityEventTracker> getLastAdjustedSamityEventBySamityAndManagementProcessId(String samityId, String managementProcessId);

	Mono<SamityEventTracker> saveSamityEventTrackerIntoHistoryAndDeleteLiveData(SamityEventTracker samityEventTracker, String loginId);

	Mono<Void> saveSamityEventTrackerIntoHistoryAndDeleteSamityEventTrackerData(SamityEventTracker samityEventTracker, String loginId);
}
