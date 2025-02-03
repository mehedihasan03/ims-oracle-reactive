package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in;

import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface ManagementProcessTrackerUseCase {
	
	Mono<ManagementProcessTracker> insertManagementProcess(String managementProcessId, String officeId, LocalDate businessDate, String loginId);
	
	Mono<ManagementProcessTracker> updateManagementProcess(String managementProcessId, String officeId, String loginId);
	Mono<String> deleteManagementProcessForOfficeByManagementProcessId(String managementProcessId, String officeId);

	Mono<String> getLastManagementProcessIdForOffice(String officeId);
	
	Mono<LocalDate> getCurrentBusinessDateForOffice(String managementProcessId, String officeId);

	Mono<LocalDate> getCurrentBusinessDateForManagementProcessId(String managementProcessId);

	Mono<ManagementProcessTracker> getLastManagementProcessForOffice(String officeId);

    Mono<List<ManagementProcessTracker>> getAllManagementProcessForOffice(String officeId);

    Mono<ManagementProcessTracker> insertManagementProcessV2(String newManagementProcessId, String mfiId, String officeId, String officeNameEn, String officeNameBn, LocalDate nextBusinessDate, String loginId);
}
