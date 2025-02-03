package net.celloscope.mraims.loanportfolio.features.withdraw.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.withdraw.adapter.out.persistence.database.entity.WithdrawEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface WithdrawRepository extends ReactiveCrudRepository<WithdrawEntity, String> {

    Flux<WithdrawEntity> findAllBySamityIdAndWithdrawTypeAndStatus(String samityId, String withdrawType, String status);
    Flux<WithdrawEntity> findAllByStagingDataId(String stagingDataId);

    Flux<WithdrawEntity> findAllByManagementProcessIdOrderBySamityId(String managementProcessId);
    Mono<Void> deleteAllByManagementProcessIdOrderBySamityId(String managementProcessId);
    Mono<WithdrawEntity> findFirstByOid(String oid);

}
