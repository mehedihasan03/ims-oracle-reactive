package net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.perisitence.repository;

import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.perisitence.entity.AisMetaDataEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AisMetaDataRepository extends ReactiveCrudRepository<AisMetaDataEntity, String> {
    Flux<AisMetaDataEntity> findAisMetaDataEntityByProcessName(String processName);
}
