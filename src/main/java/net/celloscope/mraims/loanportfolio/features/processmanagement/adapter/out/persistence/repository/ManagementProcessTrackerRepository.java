package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.ManagementProcessTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface ManagementProcessTrackerRepository extends ReactiveCrudRepository<ManagementProcessTrackerEntity, String> {
	
	Mono<ManagementProcessTrackerEntity> findFirstByOfficeIdOrderByBusinessDateDesc(String officeId);
	Flux<ManagementProcessTrackerEntity> findAllByOfficeIdOrderByBusinessDateDesc(String officeId);

	Mono<ManagementProcessTrackerEntity> findByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);

	Mono<ManagementProcessTrackerEntity> findFirstByManagementProcessId(String managementProcessId);
}
