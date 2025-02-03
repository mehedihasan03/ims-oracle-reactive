package net.celloscope.mraims.loanportfolio.features.migrationV3.interestchart;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migrationV3.MigrationUtilsV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.MigrationRequestDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationInterestChartServiceV3 {
    private final MigrationInterestChartRepositoryV3 repository;

    public Mono<InterestChart> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        log.info("Interest Chart saving started for: {}", memberRequestDto);
        return repository.findFirstBySavingsProductIdAndStatus(component.getSavingsProduct().getSavingsProductId(), MigrationEnums.STATUS_ACTIVE.getValue())
                .switchIfEmpty(
                        interestChartID()
                        .flatMap(id -> repository.save(buildInterestChart(id, memberRequestDto, requestDto, component)))
                ).doOnNext(interestChart -> {
                    log.info("Interest Chart saved: {}", interestChart);
                }).doOnError(throwable -> log.error("Error occurred while saving Interest Chart: {}", throwable.getMessage()));
    }
    private Mono<String> interestChartID() {
        return MigrationUtilsV3.generateId(
                repository.findFirstByOrderByInterestChartIdDesc(),
                InterestChart::getInterestChartId,
                "IC-",
                "IC-%04d"
        );
    }

    private InterestChart buildInterestChart(String id, MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return InterestChart.builder()
                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.INTEREST_CHART_SHORT_NAME.getValue() + "-" + UUID.randomUUID())
                .interestChartId(id)
                .savingsProductId(component.getSavingsProduct().getSavingsProductId())
                .interest(memberRequestDto.getSavingsInformation().getInterestRate())
                .interestChartValidFrDate(LocalDate.parse("2022-01-01"))
//                .interestChartValidEndDate(LocalDate.parse("2030-12-31"))
//                .balanceRangeFrom(10.00)
//                .balanceRangeTo(10000.00)
//                .accountDurationFrom(0.00)
//                .accountDurationTill(0.00)
//                .currentVersion("1")
                .mfiId(requestDto.getMfiId())
                .status(MigrationEnums.STATUS_ACTIVE.getValue())
                .migratedBy(requestDto.getLoginId())
                .migratedOn(LocalDateTime.now())
                .createdBy(requestDto.getLoginId())
                .createdOn(LocalDateTime.now())
                .build();
    }


}
