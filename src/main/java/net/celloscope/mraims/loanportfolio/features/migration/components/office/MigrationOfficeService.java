package net.celloscope.mraims.loanportfolio.features.migration.components.office;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationOfficeService {

    private final MigrationOfficeRepository repository;

    public Mono<Office> getOfficeById(String officeId) {
        return repository.findByOfficeId(officeId)
                .doOnSuccess(office -> log.info("Office Found for Office Id: {}, Office Name: {}", office.getOfficeId(), office.getOfficeNameEn()))
                .doOnError(throwable -> log.error("Error occurred while fetching Office: {}", throwable.getMessage()));
    }

}
