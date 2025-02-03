package net.celloscope.mraims.loanportfolio.features.migration.components.servicechargechart;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationServiceChargeChartRepository extends R2dbcRepository<ServiceChargeChart, String> {
    Mono<ServiceChargeChart> findFirstByOrderByServiceChargeChartIdDesc();
    Mono<ServiceChargeChart> findFirstByLoanProductIdAndStatus(String loanProductId, String status);
}
