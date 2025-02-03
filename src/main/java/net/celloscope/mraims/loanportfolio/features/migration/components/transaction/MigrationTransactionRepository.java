package net.celloscope.mraims.loanportfolio.features.migration.components.transaction;

import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

public interface MigrationTransactionRepository extends R2dbcRepository<TransactionEntity, String>{
    Flux<TransactionEntity> findAllByMemberIdInOrderByMemberIdAscCreatedOnDesc(List<String> memberId);
}
