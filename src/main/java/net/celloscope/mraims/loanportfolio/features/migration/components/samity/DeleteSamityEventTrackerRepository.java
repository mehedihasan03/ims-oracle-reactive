package net.celloscope.mraims.loanportfolio.features.migration.components.samity;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.SamityEventTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteSamityEventTrackerRepository extends ReactiveCrudRepository<SamityEventTrackerEntity, String>, DeleteDataByManagementProcessIdRepository<SamityEventTrackerEntity, String> {
	@Override
	Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
