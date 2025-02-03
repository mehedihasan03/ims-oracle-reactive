package net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsaccount;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationSavingsAccountRepositoryV3 extends R2dbcRepository<SavingsAccount, String> {
    Mono<SavingsAccount> findFirstBySavingsApplicationIdAndStatus(String savingsApplicationId, String status);

    Mono<SavingsAccount> findFirstBySavingsAccountIdLikeOrderBySavingsAccountIdDesc(String savingsAccountIdPrefix);
}
