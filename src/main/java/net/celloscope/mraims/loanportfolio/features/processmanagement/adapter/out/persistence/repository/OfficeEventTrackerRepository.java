package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OfficeEventTrackerRepository extends ReactiveCrudRepository<OfficeEventTrackerEntity, String> {
	
	Flux<OfficeEventTrackerEntity> findAllByManagementProcessIdAndOfficeIdOrderByCreatedOnAsc(String managementProcessId, String officeId);
	Flux<OfficeEventTrackerEntity> findAllByManagementProcessId(String managementProcessId);
	Mono<OfficeEventTrackerEntity> findFirstByManagementProcessIdAndOfficeIdOrderByCreatedOnDesc(String managementProcessId, String officeId);

	Mono<OfficeEventTrackerEntity> findFirstByManagementProcessIdAndOfficeIdAndOfficeEvent(String managementProcessId, String officeId, String officeEvent);
	
//	Mono<OfficeEventTrackerEntity> findFirstByManagementProcessIdAndOfficeIdAndOfficeEvent(String officeEventTrackerId, String officeId, String officeEvent);

	Mono<OfficeEventTrackerEntity> findFirstByOfficeIdOrderByCreatedOnDesc(String officeId);
}
