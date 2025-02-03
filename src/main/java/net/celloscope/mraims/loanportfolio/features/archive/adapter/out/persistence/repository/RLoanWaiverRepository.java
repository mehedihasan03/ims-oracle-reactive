package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RLoanWaiverRepository extends RestoreArchivedDataBusiness<LoanWaiverEntity, String> {
}
