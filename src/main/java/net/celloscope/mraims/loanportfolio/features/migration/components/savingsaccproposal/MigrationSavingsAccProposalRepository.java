package net.celloscope.mraims.loanportfolio.features.migration.components.savingsaccproposal;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MigrationSavingsAccProposalRepository extends R2dbcRepository<SavingsAccProposal, String> {
    Mono<SavingsAccProposal> findFistByMemberIdAndAcctStartDateAndSavingsTypeIdAndStatusOrderBySavingsApplicationIdDesc(String memberId, LocalDate accountStartDate,
                                                                                                                        String savingsTypeId, String status);
    Mono<SavingsAccProposal> findFirstByOrderBySavingsApplicationIdDesc();
    Mono<SavingsAccProposal> findFirstBySavingsApplicationIdLikeOrderBySavingsApplicationIdDesc(String savingsApplicationIdPrefix);
}
