package net.celloscope.mraims.loanportfolio.features.migration.components.lendingcategory;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface MigrationLendingCategoryRepository extends R2dbcRepository<LendingCategory, String> {
}
