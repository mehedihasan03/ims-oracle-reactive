package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionHistoryEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface LoanWriteOffCollectionHistoryRepository extends R2dbcRepository<LoanWriteOffCollectionHistoryEntity, String> {
}
