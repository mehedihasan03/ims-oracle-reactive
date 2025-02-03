package net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.processtracker;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteStagingProcessTrackerRepositoryV3 extends ReactiveCrudRepository<StagingProcessTrackerEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<StagingProcessTrackerEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
