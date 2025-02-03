package net.celloscope.mraims.loanportfolio.features.savingsclosure.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.savingsclosure.adapter.out.persistence.entity.SavingsClosureEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SavingsClosureRepository extends ReactiveCrudRepository<SavingsClosureEntity, String> {
    Mono<SavingsClosureEntity> findFirstBySavingsAccountId(String savingsAccountId);

    Mono<SavingsClosureEntity> getSavingsClosureEntityBySavingsAccountId(String savingsAccountId);

    Mono<Boolean> existsSavingsClosureEntityBySavingsAccountId(String savingsAccountId);
}
