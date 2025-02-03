package net.celloscope.mraims.loanportfolio.features.migrationV3.components.office;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationOfficeRepositoryV3 extends R2dbcRepository<Office, String>{
    Mono<Office> findByOfficeId(String officeId);
}
