package net.celloscope.mraims.loanportfolio.features.migrationV3.components.lendingcategory;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface MigrationLendingCategoryRepositoryV3 extends R2dbcRepository<LendingCategory, String> {
}
