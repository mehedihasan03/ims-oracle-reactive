package net.celloscope.mraims.loanportfolio.features.migration.components.loanaccount;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MigrationLoanAccountRepository extends R2dbcRepository<LoanAccount, String> {
    Mono<LoanAccount> findFirstByLoanApplicationIdAndStatus(String loanApplicationId, String status);
    Mono<LoanAccount> findFirstByLoanAccountIdLikeOrderByLoanAccountIdDesc(String loanAccountIdPrefix);

    Flux<LoanAccount> findAllByMemberIdAndStatus(String memberId, String status);
}
