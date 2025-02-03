package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out;

import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OfficeEventTrackerPersistencePort {
	
	Mono<OfficeEventTracker> insertOfficeEvent(OfficeEventTracker officeEventTracker);
	Mono<OfficeEventTracker> findOfficeEventTrackerByManagementProcessIdAndOfficeIdAndOfficeEvent(String managementProcessId, String officeId, String officeEvent);
	Mono<OfficeEventTracker> saveOfficeEventTrackerIntoHistory(OfficeEventTracker officeEventTracker);
	Mono<OfficeEventTracker> deleteOfficeEventTrackerForDayEndProcess(OfficeEventTracker officeEventTracker);

	Flux<OfficeEventTracker> getAllOfficeEventsForOffice(String managementProcessId, String officeId);

	Flux<OfficeEventTracker> getAllOfficeEventsForManagementProcessId(String managementProcessId);

	Mono<OfficeEventTracker> getLastOfficeEventForOffice(String managementProcessId, String officeId);

	Mono<OfficeEventTracker> getOfficeEventTrackerByStatusForOffice(String managementProcessId, String officeId, String officeEvent);
	
	Mono<Void> deleteOfficeEventForOffice(String managementProcessId, String officeId, String officeEvent);
	Mono<String> deleteOfficeEventTrackerForOffice(OfficeEventTracker officeEventTracker);

	Mono<String> getManagementProcessIdOfLastStagedOfficeEventForOffice(String officeId);
}
