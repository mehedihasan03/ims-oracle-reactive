package net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoanWaiverRepository extends ReactiveCrudRepository<LoanWaiverEntity, String> {
    Mono<LoanWaiverEntity> findFirstByLoanAccountId(String loanAccountId);

    Flux<LoanWaiverEntity> findAllBySamityId(String samityId);
    Flux<LoanWaiverEntity> findAllByManagementProcessIdOrderBySamityId(String managementProcessId);
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
    Flux<LoanWaiverEntity> findAllBySamityIdIn(List<String> samityIdList);

    @Query("""
                SELECT DISTINCT samity_id FROM template.loan_waiver_data WHERE is_locked = 'Yes' AND locked_by = :lockedBy;
            """)
    Flux<String> getSamityIdListLockedByUserForAuthorization(String lockedBy);
}
