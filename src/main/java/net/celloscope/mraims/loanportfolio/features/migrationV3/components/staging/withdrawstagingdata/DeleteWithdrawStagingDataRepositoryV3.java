package net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.withdrawstagingdata;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteWithdrawStagingDataRepositoryV3 extends ReactiveCrudRepository<StagingWithdrawDataEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<StagingWithdrawDataEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
