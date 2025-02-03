package net.celloscope.mraims.loanportfolio.features.migrationV3.components.managementprocesstracker;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.ManagementProcessTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteManagementProcessTrackerRepositoryV3 extends ReactiveCrudRepository<ManagementProcessTrackerEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<ManagementProcessTrackerEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
