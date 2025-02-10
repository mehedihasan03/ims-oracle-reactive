package net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoanAdjustmentRepository extends ReactiveCrudRepository<LoanAdjustmentDataEntity, String> {
    Flux<LoanAdjustmentDataEntity> findAllBySamityId(String samityId);

    Flux<LoanAdjustmentDataEntity> findAllByMemberIdAndStatusIsNot(String memberId, String status);

    Flux<LoanAdjustmentDataEntity> findAllBySamityIdIn(List<String> samityIdList);

    Flux<LoanAdjustmentDataEntity> findAllByManagementProcessIdAndSamityId(String managementProcessId, String samityId);

    @Query("""
            SELECT DISTINCT samity_id FROM template.loan_adjustment_data csd WHERE is_locked = 'Yes' AND locked_by = :lockedBy;
            """)
    Flux<String> getSamityIdListLockedByUserForAuthorization(String lockedBy);

    Mono<LoanAdjustmentDataEntity> findFirstByLoanAccountId(String loanAccountId);

    Flux<LoanAdjustmentDataEntity> findAllByManagementProcessId(String managementProcessId);

    Flux<LoanAdjustmentDataEntity> findAllByManagementProcessIdAndProcessId(String managementProcessId, String processId);

    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);

    Mono<Void> deleteAllByManagementProcessIdAndLoanAdjustmentProcessId(String managementProcessId, String loanAdjustmentProcessId);

    @Query("""
            SELECT * FROM template.loan_adjustment_data WHERE management_process_id = :managementProcessId and loan_account_id is not null and case when (:loginId is not null and :loginId != '') then (created_by = :loginId) else 1 = 1 end LIMIT :limit OFFSET :offset;
            """)
    Flux<LoanAdjustmentDataEntity> findAllByManagementProcessIdAndCreatedBy(String managementProcessId, String loginId, int limit, int offset);


    @Query("""
            SELECT count(*) FROM template.loan_adjustment_data WHERE management_process_id = :managementProcessId and loan_account_id is not null and case when (:loginId is not null and :loginId != '') then (created_by = :loginId) else 1 = 1 end;
            """)
    Mono<Long> getCountLoanDataEntity(String managementProcessId, String loginId);

    Flux<LoanAdjustmentDataEntity> findAllByManagementProcessIdAndLoanAdjustmentProcessId(String managementProcessId, String loanAdjustmentProcessId);

    Mono<LoanAdjustmentDataEntity> findByOid(String oid);

    Mono<LoanAdjustmentDataEntity> findFirstBySavingsAccountId(String savingsAccountId);

    Flux<LoanAdjustmentDataEntity> findAllByOidIn(List<String> oidList);

    Mono<Void> deleteAllByManagementProcessIdAndProcessId(String managementProcessId, String processId);

    Mono<Long> countByManagementProcessIdAndSamityId(String managementProcessId, String samityId);
}
