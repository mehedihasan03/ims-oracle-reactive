package net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsproduct;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationSavingsProductRepositoryV3 extends R2dbcRepository<SavingsProduct, String> {

    Mono<SavingsProduct> findFirstBySavingsProdNameEnOrderBySavingsProductId(String savingsProductNameEn);
    Mono<SavingsProduct> findFirstByOrderBySavingsProductIdDesc();
}
