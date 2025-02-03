package net.celloscope.mraims.loanportfolio.features.migrationV3.components.transaction;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationTransactionServiceV3 {
    private final MigrationTransactionRepositoryV3 repository;

    public Flux<TransactionEntity> getByMemberIdList(List<String> memberIdList) {
        return repository.findAllByMemberIdInOrderByMemberIdAscCreatedOnDesc(memberIdList)
                .doOnNext(transactionEntity -> log.info("TransactionEntity: {}", transactionEntity));
    }
}
