package net.celloscope.mraims.loanportfolio.features.migration.components.staging.stagingdata;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface MigrationStagingDataRepository extends R2dbcRepository<StagingDataEntity, String>{
    Flux<StagingDataEntity> findByManagementProcessIdOrderByMemberIdDesc(String managementProcessId);
}
