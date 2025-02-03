package net.celloscope.mraims.loanportfolio.features.migration.interestchart;

import net.celloscope.mraims.loanportfolio.features.migration.components.servicechargechart.ServiceChargeChart;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationInterestChartRepository extends R2dbcRepository<InterestChart, String> {
    Mono<InterestChart> findFirstByOrderByInterestChartIdDesc();
    Mono<InterestChart> findFirstBySavingsProductIdAndStatus(String savingsProductId, String status);
}
