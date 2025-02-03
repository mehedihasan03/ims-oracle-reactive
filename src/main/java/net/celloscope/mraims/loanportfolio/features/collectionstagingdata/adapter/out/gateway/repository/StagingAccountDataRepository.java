package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.entity.StagingAccountDataEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StagingAccountDataRepository extends R2dbcRepository<StagingAccountDataEntity, String> {
    Flux<StagingAccountDataEntity> findAllByMemberIdIn(List<String> memberId);

    Mono<StagingAccountDataEntity> findByLoanAccountIdAndManagementProcessId(String loanAccountId, String managementProcessId);

}
