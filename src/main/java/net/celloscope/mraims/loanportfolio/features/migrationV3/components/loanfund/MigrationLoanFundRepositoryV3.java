package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanfund;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface MigrationLoanFundRepositoryV3 extends R2dbcRepository<LoanFund, String> {
}
