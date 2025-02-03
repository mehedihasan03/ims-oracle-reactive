package net.celloscope.mraims.loanportfolio.features.migrationV3.components.samity;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationSamityRepositoryV3 extends R2dbcRepository<Samity, String> {
    Mono<Samity> findBySamityId(String samityId);
}
