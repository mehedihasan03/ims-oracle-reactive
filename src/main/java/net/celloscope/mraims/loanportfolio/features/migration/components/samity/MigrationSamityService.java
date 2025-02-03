package net.celloscope.mraims.loanportfolio.features.migration.components.samity;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationSamityService {
    private final MigrationSamityRepository repository;

    public Mono<Samity> getSamityById(String samityId) {
        return repository.findBySamityId(samityId)
                .doOnNext(samity -> log.info("Samity Found for Samity Id: {}, Samity Name: {}", samity.getSamityId(), samity.getSamityNameEn()))
                .doOnError(throwable -> log.error("Error occurred while fetching Samity: {}", throwable.getMessage()));
    }
}
