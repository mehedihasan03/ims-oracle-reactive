package net.celloscope.mraims.loanportfolio.features.migration.components.managementprocesstracker;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.ManagementProcessTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteManagementProcessTrackerRepository extends ReactiveCrudRepository<ManagementProcessTrackerEntity, String>, DeleteDataByManagementProcessIdRepository<ManagementProcessTrackerEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
