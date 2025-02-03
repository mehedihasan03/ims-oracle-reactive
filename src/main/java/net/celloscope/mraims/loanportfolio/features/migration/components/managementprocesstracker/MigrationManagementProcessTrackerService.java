package net.celloscope.mraims.loanportfolio.features.migration.components.managementprocesstracker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationUtils;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedCollectionResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.ManagementProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.repository.ManagementProcessTrackerRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class MigrationManagementProcessTrackerService {
    private final ManagementProcessTrackerRepository managementProcessTrackerRepository;

    public Mono<ManagementProcessTrackerEntity> getByOfficeId(String officeId) {
        return managementProcessTrackerRepository.findFirstByOfficeIdOrderByBusinessDateDesc(officeId)
                .doOnRequest(value -> log.info("Request: {}", officeId))
                .doOnNext(managementProcessTracker -> log.info("ManagementProcessTracker: {}", managementProcessTracker))
                .doOnError(throwable -> log.error("Error occurred while fetching ManagementProcessTracker: {}", throwable.getMessage()));
    }

    public Mono<ManagementProcessTrackerEntity> saveOnToCutOfDate(MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return managementProcessTrackerRepository.findFirstByOfficeIdOrderByBusinessDateDesc(requestDto.getOfficeId())
                        .switchIfEmpty(managementProcessTrackerRepository.save(buildManagementProcessTrackerEntity(requestDto, component)))
                .doOnNext(managementProcessTracker -> log.info("ManagementProcessTracker: {}", managementProcessTracker))
                .doOnError(throwable -> log.error("Error occurred while saving ManagementProcessTracker: {}", throwable.getMessage()));
    }

    private ManagementProcessTrackerEntity buildManagementProcessTrackerEntity(MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return ManagementProcessTrackerEntity.builder()
//                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + UUID.randomUUID())
                .managementProcessId(MigrationEnums.MIGRATION.getValue()+ "-" + UUID.randomUUID())
                .officeId(requestDto.getOfficeId())
                .officeNameEn(component.getOffice().getOfficeNameEn())
                .officeNameBn(component.getOffice().getOfficeNameBn())
                .mfiId(requestDto.getMfiId())
                .businessDate(requestDto.getConfigurations().getCutOffDate())
                .businessDay(MigrationUtils.getWeekday(requestDto.getConfigurations().getCutOffDate()))
                .createdOn(LocalDateTime.now())
                .createdBy(requestDto.getLoginId())
                .build();
    }

}
