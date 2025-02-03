package net.celloscope.mraims.loanportfolio.features.migration.components.loanproduct;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationLoanProductRepository extends R2dbcRepository<LoanProduct, String> {
    Mono<LoanProduct> findFirstByOrderByLoanProductIdDesc();
    Mono<LoanProduct> findByLoanProductNameEn(String loanProductName);
}
