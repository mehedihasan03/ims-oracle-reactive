package net.celloscope.mraims.loanportfolio.features.migration.components.samity;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationSamityRepository extends R2dbcRepository<Samity, String> {
    Mono<Samity> findBySamityId(String samityId);
}
