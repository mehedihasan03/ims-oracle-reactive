package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ILoanWaiverHistoryRepositoryDelete extends ReactiveCrudRepository<LoanWaiverHistoryEntity, String> , DeleteArchiveDataBusiness<LoanWaiverHistoryEntity,String> {
}
