package net.celloscope.mraims.loanportfolio.features.migration.components.officeeventtracker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedCollectionResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationCollectionRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository.OfficeEventTrackerRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationOfficeEventTrackerService {

    private final OfficeEventTrackerRepository repository;

    public Mono<OfficeEventTrackerEntity> getByOfficeId(MigrationCollectionRequestDto requestDto, MigratedCollectionResponseDto responseDto) {
        return repository.findFirstByManagementProcessIdAndOfficeIdAndOfficeEvent(responseDto.getManagementProcessTracker().getManagementProcessId(),
                requestDto.getOfficeId(), OfficeEvents.DAY_STARTED.getValue())
                .doOnNext(officeEventTracker -> log.info("OfficeEventTracker: {}", officeEventTracker))
                .doOnError(throwable -> log.error("Error occurred while fetching OfficeEventTracker: {}", throwable.getMessage()));
    }

    public Mono<OfficeEventTrackerEntity> save(MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return repository.findFirstByManagementProcessIdAndOfficeIdAndOfficeEvent(component.getManagementProcessTracker().getManagementProcessId(),
                    requestDto.getOfficeId(), OfficeEvents.DAY_STARTED.getValue())
                .switchIfEmpty(repository.save(buildOfficeEventTracker(requestDto, component)))
                .doOnNext(officeEventTracker -> log.info("OfficeEventTracker: {}", officeEventTracker))
                .doOnError(throwable -> log.error("Error occurred while saving OfficeEventTracker: {}", throwable.getMessage()));
    }

    private OfficeEventTrackerEntity buildOfficeEventTracker(MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return OfficeEventTrackerEntity.builder()
//                .oid(MigrationEnums.MIGRATION.getValue() + "-" + UUID.randomUUID())
                .managementProcessId(component.getManagementProcessTracker().getManagementProcessId())
                .officeEventTrackerId(MigrationEnums.MIGRATION.getValue() + "-" + UUID.randomUUID())
                .officeId(requestDto.getOfficeId())
                .officeEvent(OfficeEvents.DAY_STARTED.getValue())
                .createdOn(LocalDateTime.now())
                .createdBy(requestDto.getLoginId())
                .build();
    }
}
