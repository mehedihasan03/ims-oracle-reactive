package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RLoanAdjustmentRepository extends RestoreArchivedDataBusiness<LoanAdjustmentDataEntity, String> {
}
