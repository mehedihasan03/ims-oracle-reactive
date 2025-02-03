package net.celloscope.mraims.loanportfolio.features.migrationV3.components.transaction;

import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface MigrationTransactionRepositoryV3 extends R2dbcRepository<TransactionEntity, String>{
    Flux<TransactionEntity> findAllByMemberIdInOrderByMemberIdAscCreatedOnDesc(List<String> memberId);
}
