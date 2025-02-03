package net.celloscope.mraims.loanportfolio.features.migration.components.loanfund;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface MigrationLoanFundRepository extends R2dbcRepository<LoanFund, String> {
}
