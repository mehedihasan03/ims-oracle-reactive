package net.celloscope.mraims.loanportfolio.features.migration.components.servicechargechart;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationEnums;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationUtils;
import net.celloscope.mraims.loanportfolio.features.migration.components.loanapplication.LoanApplication;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedComponentsResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationMemberRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class MigrationServiceChargeChartService {
    private final MigrationServiceChargeChartRepository repository;

    public Mono<ServiceChargeChart> save(MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return repository.findFirstByLoanProductIdAndStatus(component.getLoanProduct().getLoanProductId(), MigrationEnums.STATUS_ACTIVE.getValue())
                .switchIfEmpty(
                    serviceChargeChartID()
                        .flatMap(id -> repository.save(buildServiceChargeChart(id, memberRequestDto, requestDto, component)))
                ).doOnError(throwable -> log.error("Error occurred while saving ServiceChargeChart: {}", throwable.getMessage()));
    }
    private Mono<String> serviceChargeChartID() {
        return MigrationUtils.generateId(
                repository.findFirstByOrderByServiceChargeChartIdDesc(),
                ServiceChargeChart::getServiceChargeChartId,
                "SC-",
                "SC-%04d"
        );
    }

    private ServiceChargeChart buildServiceChargeChart(String id, MigrationMemberRequestDto memberRequestDto, MigrationRequestDto requestDto, MigratedComponentsResponseDto component) {
        return ServiceChargeChart.builder()
                .oid(MigrationEnums.MIGRATION.getValue()+ "-" + MigrationEnums.SERVICE_CHARGE_CHART_SHORT_NAME.getValue() + "-" + UUID.randomUUID())                // This is the primary key
                .serviceChargeChartId(id)   // *Required
                .loanProductId(component.getLoanProduct().getLoanProductId())         // *Required
                .serviceChargeRate(memberRequestDto.getLoanInformation().getServiceChargeRate())  // *Required
                .serviceChargeRateFreq(memberRequestDto.getLoanInformation().getServiceChargeRateFreq()) // *Required
//                .scChartValidFrDate(LocalDate.now())
//                .scChartValidEndDate(LocalDate.now())
//                .amountFrom(BigDecimal.ONE)
//                .amountTo(BigDecimal.TEN)
//                .description("Description")
//                .currentVersion("1.0")
//                .isNewRecord("Yes")
//                .approvedBy("User")
//                .approvedOn(LocalDateTime.now())
//                .remarkedBy("User")
//                .remarkedOn(LocalDateTime.now())
//                .isApproverRemarks("Yes")
//                .approverRemarks("Approver Remarks")
                .mfiId(requestDto.getMfiId())            // *Required
                .status(MigrationEnums.STATUS_ACTIVE.getValue())   // *Required
                .migratedBy(requestDto.getLoginId()) // *Required
                .migratedOn(LocalDateTime.now())   // *Required
                .createdBy(requestDto.getLoginId())  // *Required
                .createdOn(LocalDateTime.now())    // *Required
//                .updatedBy("User")
//                .updatedOn(LocalDateTime.now())
//                .closedBy("User")
//                .closedOn(LocalDateTime.now())
                .build();
    }


}
