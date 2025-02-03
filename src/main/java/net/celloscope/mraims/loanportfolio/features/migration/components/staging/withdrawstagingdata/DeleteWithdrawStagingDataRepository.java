package net.celloscope.mraims.loanportfolio.features.migration.components.staging.withdrawstagingdata;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteWithdrawStagingDataRepository extends ReactiveCrudRepository<StagingWithdrawDataEntity, String>, DeleteDataByManagementProcessIdRepository<StagingWithdrawDataEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
