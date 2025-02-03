package net.celloscope.mraims.loanportfolio.features.migrationV3.components.samity;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.SamityEventTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteSamityEventTrackerRepositoryV3 extends ReactiveCrudRepository<SamityEventTrackerEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<SamityEventTrackerEntity, String> {
	@Override
	Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
