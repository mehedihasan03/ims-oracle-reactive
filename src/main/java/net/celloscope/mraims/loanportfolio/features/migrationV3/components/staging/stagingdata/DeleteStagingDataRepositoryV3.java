package net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.stagingdata;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DeleteStagingDataRepositoryV3 extends R2dbcRepository<StagingDataEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<StagingDataEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
