package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.entity.SavingsAccountInterestDepositHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingsAccountInterestDepositHistoryRepository extends ReactiveCrudRepository<SavingsAccountInterestDepositHistoryEntity, String>{
}
