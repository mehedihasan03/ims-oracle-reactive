package net.celloscope.mraims.loanportfolio.features.migrationV3.interestchart;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface MigrationInterestChartRepositoryV3 extends R2dbcRepository<InterestChart, String> {
    Mono<InterestChart> findFirstByOrderByInterestChartIdDesc();
    Mono<InterestChart> findFirstBySavingsProductIdAndStatus(String savingsProductId, String status);
}
