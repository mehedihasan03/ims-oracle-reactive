package net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LoanWaiverHistoryRepository extends ReactiveCrudRepository<LoanWaiverHistoryEntity, String> {

    Mono<LoanWaiverHistoryEntity> findFirstByLoanWaiverDataOidOrderByCreatedOnDesc(String loanWaiverOid);
}
