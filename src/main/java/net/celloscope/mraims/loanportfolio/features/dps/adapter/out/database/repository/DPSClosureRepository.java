package net.celloscope.mraims.loanportfolio.features.dps.adapter.out.database.repository;

import net.celloscope.mraims.loanportfolio.features.dps.adapter.out.database.entity.DPSClosureEntity;
import net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.entity.FDRClosureEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DPSClosureRepository extends ReactiveCrudRepository<DPSClosureEntity, String> {
    Mono<DPSClosureEntity> getDPSClosureEntityBySavingsAccountId(String savingsAccountId);
    Mono<Boolean> existsDPSClosureEntityBySavingsAccountId(String savingsAccountId);
    Flux<DPSClosureEntity> getAllByOfficeId(String officeId);
}
