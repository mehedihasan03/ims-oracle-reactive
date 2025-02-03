package net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.stagingaccountdata;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DeleteStagingAccountDataRepositoryV3 extends R2dbcRepository<StagingAccountDataEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<StagingAccountDataEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
