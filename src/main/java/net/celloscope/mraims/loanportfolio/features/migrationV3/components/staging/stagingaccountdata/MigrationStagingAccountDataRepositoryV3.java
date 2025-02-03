package net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.stagingaccountdata;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface MigrationStagingAccountDataRepositoryV3 extends R2dbcRepository<StagingAccountDataEntity, String>{
    Flux<StagingAccountDataEntity> findByManagementProcessIdOrderByMemberIdDesc(String managementProcessId);
}
