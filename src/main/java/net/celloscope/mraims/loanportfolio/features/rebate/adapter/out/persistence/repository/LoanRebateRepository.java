package net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanRebateRepository extends R2dbcRepository<LoanRebateEntity, String> {
    @Query("""
    SELECT * FROM loan_rebate_data lrd WHERE lrd.samity_id LIKE :officeId || '%' AND (:startDate IS NULL OR :endDate IS NULL OR lrd.created_on BETWEEN :startDate AND :endDate);
    """)
    Flux<LoanRebateEntity> getLoanRebateDataByOfficeIdInASpecificDateRange(String officeId, LocalDateTime startDate, LocalDateTime endDate);

    Flux<LoanRebateEntity> findAllByManagementProcessIdOrderBySamityId(String managementProcessId);
    Mono<Void>deleteAllByManagementProcessId(String managementProcessId);

    @Query("""
    SELECT DISTINCT samity_id FROM loan_rebate_data WHERE is_locked = 'Yes' AND locked_by = :lockedBy;
    """)
    Flux<String> getSamityIdListLockedByUserForAuthorization(String lockedBy);
    Flux<LoanRebateEntity> findAllBySamityIdIn(List<String> samityIdList);

    @Query("""
    update loan_rebate_data
    set approved_on = :approvedOn, approved_by = :loginId, status = :status, locked_by = null, locked_on = null, is_locked = 'No', edit_commit = 'No'
    where oid = :oid; """)
    Flux<LoanRebateEntity> updateLoanRebateEntitiesForAuthorization(String oid, String loginId, LocalDateTime approvedOn, String status);

    Mono<LoanRebateEntity> getLoanRebateEntityByLoanRebateDataId(String loanRebateDataId);

    Mono<LoanRebateEntity> findByLoanAccountId(String loanAccountId);
}
