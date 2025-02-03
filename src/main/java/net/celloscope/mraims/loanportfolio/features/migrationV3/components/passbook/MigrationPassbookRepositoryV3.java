package net.celloscope.mraims.loanportfolio.features.migrationV3.components.passbook;

import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.PassbookEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface MigrationPassbookRepositoryV3 extends R2dbcRepository<PassbookEntity, String>{
    Flux<PassbookEntity> findAllByMemberIdInOrderByMemberIdAscCreatedOnDesc(List<String> memberId);
}
