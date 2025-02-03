package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEditHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface IWithdrawStagingDataEditHistoryRepository extends ReactiveCrudRepository<StagingWithdrawDataEditHistoryEntity, String> {
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
