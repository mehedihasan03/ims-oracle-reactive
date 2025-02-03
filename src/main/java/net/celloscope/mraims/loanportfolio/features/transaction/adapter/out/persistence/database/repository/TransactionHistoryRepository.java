package net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionHistoryRepository extends ReactiveCrudRepository<TransactionHistoryEntity, String>{
}
