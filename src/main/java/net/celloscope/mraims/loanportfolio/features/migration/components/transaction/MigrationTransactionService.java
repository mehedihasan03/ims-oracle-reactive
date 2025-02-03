package net.celloscope.mraims.loanportfolio.features.migration.components.transaction;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationTransactionService {
    private final MigrationTransactionRepository repository;

    public Flux<TransactionEntity> getByMemberIdList(List<String> memberIdList) {
        return repository.findAllByMemberIdInOrderByMemberIdAscCreatedOnDesc(memberIdList)
                .doOnNext(transactionEntity -> log.info("TransactionEntity: {}", transactionEntity));
    }
}
