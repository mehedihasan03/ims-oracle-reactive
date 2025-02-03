package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RLoanRebateRepository extends RestoreArchivedDataBusiness<LoanRebateEntity, String> {
}
