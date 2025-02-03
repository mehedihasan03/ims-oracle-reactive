package net.celloscope.mraims.loanportfolio.features.migration.components.person;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationPersonRepository extends R2dbcRepository<Person, String> {

    Mono<Boolean> existsByPersonId(String personId);
    Mono<Person> findByPersonId(String personId);
}
