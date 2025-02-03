package net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEditHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LoanRebateEditHistoryRepository extends ReactiveCrudRepository<LoanRebateEditHistoryEntity, String> {
    Mono<LoanRebateEditHistoryEntity> findTopByLoanRebateDataIdOrderByCreatedOnDesc(String loanRebateDataId);
}
