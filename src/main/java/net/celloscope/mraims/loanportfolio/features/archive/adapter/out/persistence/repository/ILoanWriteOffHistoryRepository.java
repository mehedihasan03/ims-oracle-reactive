package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ILoanWriteOffHistoryRepository extends ReactiveCrudRepository<LoanWriteOffCollectionHistoryEntity, String>, DeleteArchiveDataBusiness<LoanWriteOffCollectionHistoryEntity,String> {
}
