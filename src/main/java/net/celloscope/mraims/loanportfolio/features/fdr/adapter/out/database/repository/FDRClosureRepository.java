package net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.repository;

import net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.entity.FDRClosureEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FDRClosureRepository extends ReactiveCrudRepository<FDRClosureEntity, String> {
    Mono<FDRClosureEntity> getFDRClosureEntityBySavingsAccountId(String savingsAccountId);
    Mono<Boolean> existsFDRClosureEntityBySavingsAccountId(String savingsAccountId);
    Flux<FDRClosureEntity> findAllByOfficeId(String officeId);
}
