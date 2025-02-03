package net.celloscope.mraims.loanportfolio.features.migrationV3.components.officeeventtracker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigratedCollectionResponseDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationCollectionRequestDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationRequestDto;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository.OfficeEventTrackerRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationOfficeEventTrackerServiceV3 {

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

    public Mono<OfficeEventTrackerEntity> insertOfficeEvents(MigratedCollectionResponseDto migratedCollectionResponseDto, MigrationCollectionRequestDto requestDto) {
        String managementProcessId = migratedCollectionResponseDto.getManagementProcessTracker().getManagementProcessId();
        String officeId = migratedCollectionResponseDto.getOffice().getOfficeId();
        return repository.findFirstByManagementProcessIdAndOfficeIdOrderByCreatedOnDesc(managementProcessId, officeId)
                .switchIfEmpty(Mono.error(new RuntimeException("No office event tracker found for the given management process id: " + managementProcessId + "and office id: " + officeId)))
                .flatMap(officeEventTrackerEntity -> {
                    OfficeEventTrackerEntity autoVoucherEvent = OfficeEventTrackerEntity.builder()
                            .managementProcessId(officeEventTrackerEntity.getManagementProcessId())
                            .officeEventTrackerId(MigrationEnums.MIGRATION.getValue() + "-" + UUID.randomUUID())
                            .officeId(officeEventTrackerEntity.getOfficeId())
                            .officeEvent(OfficeEvents.AUTO_VOUCHER_GENERATION_COMPLETED.getValue())
                            .createdOn(LocalDateTime.now())
                            .createdBy(requestDto.getLoginId())
                            .build();

                    OfficeEventTrackerEntity dayEndEvent = OfficeEventTrackerEntity.builder()
                            .managementProcessId(officeEventTrackerEntity.getManagementProcessId())
                            .officeEventTrackerId(MigrationEnums.MIGRATION.getValue() + "-" + UUID.randomUUID())
                            .officeId(officeEventTrackerEntity.getOfficeId())
                            .officeEvent(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())
                            .createdOn(LocalDateTime.now())
                            .createdBy(requestDto.getLoginId())
                            .build();

                    OfficeEventTrackerEntity monthEndEvent = OfficeEventTrackerEntity.builder()
                            .managementProcessId(officeEventTrackerEntity.getManagementProcessId())
                            .officeEventTrackerId(MigrationEnums.MIGRATION.getValue() + "-" + UUID.randomUUID())
                            .officeId(officeEventTrackerEntity.getOfficeId())
                            .officeEvent(OfficeEvents.MONTH_END_PROCESS_COMPLETED.getValue())
                            .createdOn(LocalDateTime.now())
                            .createdBy(requestDto.getLoginId())
                            .build();

                    return repository
                            .save(autoVoucherEvent)
                            .doOnSuccess(officeEventTrackerEntity1 -> log.info("AutoVoucher event saved successfully: {}", officeEventTrackerEntity1))
                            .then(repository.save(dayEndEvent))
                            .doOnSuccess(officeEventTrackerEntity1 -> log.info("DayEnd event saved successfully: {}", officeEventTrackerEntity1))
                            .then(repository.save(monthEndEvent))
                            .doOnSuccess(officeEventTrackerEntity1 -> log.info("MonthEnd event saved successfully: {}", officeEventTrackerEntity1))
                            .thenReturn(officeEventTrackerEntity);
                })
                .doOnError(throwable -> log.error("Error occurred while saving DayEnd & MonthEnd office events : {}", throwable.getMessage()));
    }
}
