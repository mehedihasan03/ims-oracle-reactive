package net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEditHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanAdjustmentEditHistoryRepository extends ReactiveCrudRepository<LoanAdjustmentDataEditHistoryEntity, String> {
}
