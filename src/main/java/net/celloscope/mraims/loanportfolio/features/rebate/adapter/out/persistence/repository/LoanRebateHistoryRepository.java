package net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateHistoryEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanRebateHistoryRepository extends R2dbcRepository<LoanRebateHistoryEntity, String> {
    Mono<LoanRebateHistoryEntity> findTopByLoanRebateDataOidOrderByCreatedOnDesc(String loanRebateDataOid);
}
