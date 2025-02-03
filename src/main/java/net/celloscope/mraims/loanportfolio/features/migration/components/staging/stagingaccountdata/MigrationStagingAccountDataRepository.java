package net.celloscope.mraims.loanportfolio.features.migration.components.staging.stagingaccountdata;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface MigrationStagingAccountDataRepository extends R2dbcRepository<StagingAccountDataEntity, String>{
    Flux<StagingAccountDataEntity> findByManagementProcessIdOrderByMemberIdDesc(String managementProcessId);
}
