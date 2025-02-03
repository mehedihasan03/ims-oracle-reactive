package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.out;

import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface ManagementProcessTrackerPersistencePort {
	
	Mono<ManagementProcessTracker> insertManagementProcess(ManagementProcessTracker managementProcessTracker);

	Mono<String> deleteManagementProcessByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);
	
	Mono<String> getLastManagementProcessIdForOffice(String officeId);
	
	Mono<LocalDate> getCurrentBusinessDateForOffice(String managementProcessId, String officeId);
	
	Mono<LocalDate> getCurrentBusinessDateByManagementProcessId(String managementProcessId);
	
	Mono<ManagementProcessTracker> getLastManagementProcessForOffice(String officeId);

    Flux<ManagementProcessTracker> getAllManagementProcessForOffice(String officeId);
}
