package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in;

import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OfficeEventTrackerUseCase {
	
	Mono<OfficeEventTracker> insertOfficeEvent(String managementProcessId, String officeId, String officeEvent, String loginId, String officeEventTrackerId);

	Mono<OfficeEventTracker> updateOfficeEvent(String managementProcessId, String officeEventTrackerId, String officeId, String officeEvent, String loginId);
	
	Mono<OfficeEventTracker> getLastOfficeEventForOffice(String managementProcessId, String officeId);
	
	Flux<OfficeEventTracker> getAllOfficeEventsForOffice(String managementProcessId, String officeId);

	Mono<List<OfficeEventTracker>> getAllOfficeEventsForManagementProcessId(String managementProcessId);

	Mono<Void> deleteOfficeEventForOffice(String managementProcessId, String officeId, String officeEvent);

	Mono<String> deleteOfficeEventTracker(OfficeEventTracker officeEventTracker);

	Mono<OfficeEventTracker> getOfficeEventByStatusForOffice(String managementProcessId, String officeId, String officeEvent);

	Mono<String> getManagementProcessIdOfLastStagedOfficeEventForOffice(String officeId);
}
