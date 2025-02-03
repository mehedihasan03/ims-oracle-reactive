package net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository;

//import net.celloscope.mraims.loanportfolio.features.cancel.adapter.out.entity.SamityEventTrackerEntity;

import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.SamityEventTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SamityEventTrackerRepository extends ReactiveCrudRepository<SamityEventTrackerEntity, String> {
	
	Flux<SamityEventTrackerEntity> findByManagementProcessIdAndSamityIdOrderByCreatedOnAsc(String managementProcessId, String samityId);

	Mono<SamityEventTrackerEntity> findFirstByManagementProcessIdAndSamityIdAndSamityEvent(String managementProcessId, String samityId, String samityEvent);

	Mono<Void> deleteBySamityEventTrackerIdIn(List<String> samityEventTrackerIdList);

    Flux<SamityEventTrackerEntity> findAllByManagementProcessIdAndOfficeIdOrderBySamityId(String managementProcessId, String officeId);

	Mono<Void> deleteByOid(String oid);

}
