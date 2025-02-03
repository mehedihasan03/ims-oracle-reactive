package net.celloscope.mraims.loanportfolio.features.migration.components.loanapplication;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MigrationLoanApplicationRepository extends ReactiveCrudRepository<LoanApplication, String> {
    Mono<LoanApplication> findFirstByMemberIdAndAppliedLoanAmountAndExpectedDisburseDtAndStatusOrderByLoanApplicationIdDesc(String memberId, BigDecimal appliedLoanAmount,
                                                                                                                            LocalDate expectedDisburseDt, String status);
    Mono<LoanApplication> findFirstByOrderByLoanApplicationIdDesc();
    Mono<LoanApplication> findFirstByLoanApplicationIdLikeOrderByLoanApplicationIdDesc(String loanApplicationId, String status);
}
