package net.celloscope.mraims.loanportfolio.features.migration.components.office;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationOfficeRepository extends R2dbcRepository<Office, String>{
    Mono<Office> findByOfficeId(String officeId);
}
