package net.celloscope.mraims.loanportfolio.features.migration.components.passbook;

import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.PassbookEntity;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface MigrationPassbookRepository extends R2dbcRepository<PassbookEntity, String>{
    Flux<PassbookEntity> findAllByMemberIdInOrderByMemberIdAscCreatedOnDesc(List<String> memberId);
}
