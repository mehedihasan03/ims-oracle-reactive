package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ILoanRebateHistoryRepositoryDelete extends ReactiveCrudRepository<LoanRebateHistoryEntity, String>, DeleteArchiveDataBusiness<LoanRebateHistoryEntity,String> {
}
