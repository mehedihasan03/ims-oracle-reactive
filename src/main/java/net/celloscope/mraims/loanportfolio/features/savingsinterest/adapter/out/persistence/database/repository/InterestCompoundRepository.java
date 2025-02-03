package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.out.persistence.database.entity.SavingsInterestCompoundEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface InterestCompoundRepository extends ReactiveCrudRepository<SavingsInterestCompoundEntity, String>{
}
