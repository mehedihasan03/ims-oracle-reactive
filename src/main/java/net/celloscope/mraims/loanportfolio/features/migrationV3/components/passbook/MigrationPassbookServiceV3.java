package net.celloscope.mraims.loanportfolio.features.migrationV3.components.passbook;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.PassbookEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationPassbookServiceV3 {
    private final MigrationPassbookRepositoryV3 repository;
    public Flux<PassbookEntity> getByMemberIdList(List<String> memberIdList) {
        return repository.findAllByMemberIdInOrderByMemberIdAscCreatedOnDesc(memberIdList)
            .doOnNext(passbookEntities -> log.info("Passbook Entities: {}", passbookEntities));
    }
}
