package net.celloscope.mraims.loanportfolio.features.migration.components.staging.stagingaccountdata;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeleteStagingAccountDataRepository extends R2dbcRepository<StagingAccountDataEntity, String>, DeleteDataByManagementProcessIdRepository<StagingAccountDataEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
