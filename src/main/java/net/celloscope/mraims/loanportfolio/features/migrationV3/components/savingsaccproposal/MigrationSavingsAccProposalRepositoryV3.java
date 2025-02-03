package net.celloscope.mraims.loanportfolio.features.migrationV3.components.savingsaccproposal;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface MigrationSavingsAccProposalRepositoryV3 extends R2dbcRepository<SavingsAccProposal, String> {
    Mono<SavingsAccProposal> findFistByMemberIdAndAcctStartDateAndSavingsTypeIdAndStatusOrderBySavingsApplicationIdDesc(String memberId, LocalDate accountStartDate,
                                                                                                                        String savingsTypeId, String status);
    Mono<SavingsAccProposal> findFirstByOrderBySavingsApplicationIdDesc();
    Mono<SavingsAccProposal> findFirstBySavingsApplicationIdLikeOrderBySavingsApplicationIdDesc(String savingsApplicationIdPrefix);
}
