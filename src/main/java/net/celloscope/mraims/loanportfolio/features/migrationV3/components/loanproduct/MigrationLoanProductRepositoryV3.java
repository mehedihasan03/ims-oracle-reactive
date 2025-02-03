package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanproduct;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationLoanProductRepositoryV3 extends R2dbcRepository<LoanProduct, String> {
    Mono<LoanProduct> findFirstByOrderByLoanProductIdDesc();
    Mono<LoanProduct> findByLoanProductNameEn(String loanProductName);
}
