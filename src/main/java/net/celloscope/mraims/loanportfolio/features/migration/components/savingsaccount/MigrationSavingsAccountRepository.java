package net.celloscope.mraims.loanportfolio.features.migration.components.savingsaccount;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationSavingsAccountRepository extends R2dbcRepository<SavingsAccount, String> {
    Mono<SavingsAccount> findFirstBySavingsApplicationIdAndStatus(String savingsApplicationId, String status);

    Mono<SavingsAccount> findFirstBySavingsAccountIdLikeOrderBySavingsAccountIdDesc(String savingsAccountIdPrefix);
}
