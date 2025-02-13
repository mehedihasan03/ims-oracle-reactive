package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.withdraw.adapter.out.persistence.database.entity.WithdrawEntity;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

public interface IWithdrawStagingDataRepository extends ReactiveCrudRepository<StagingWithdrawDataEntity, String> {

    Mono<StagingWithdrawDataEntity> findFirstByManagementProcessIdAndSavingsAccountId(String managementProcessId, String savingsAccountId);

    Mono<StagingWithdrawDataEntity> findWithdrawStagingDataEntityBySavingsAccountId(String savingsAccountId);

    Flux<StagingWithdrawDataEntity> findWithdrawStagingDataEntitiesBySamityIdAndWithdrawType(String samityId, String withdrawType);

    @Query("""
        SELECT
        	sum(amount)
        FROM
        	template.staging_withdraw_data swd
        WHERE
        	swd.samity_id = :samityId;
    """)
    Mono<BigDecimal> getTotalWithdrawAmountOfASamity(String samityId);

    Flux<StagingWithdrawDataEntity> findAllBySamityIdOrderByCreatedOn(String samityId);

    Flux<StagingWithdrawDataEntity> findAllBySavingsAccountIdIn(List<String> savingsAccountId);

    Flux<StagingWithdrawDataEntity> findAllByManagementProcessIdAndSamityId(String managementProcessId, String samityId);

    Flux<StagingWithdrawDataEntity> findAllBySamityId(String samityId);

    @Query("""
        SELECT DISTINCT samity_id FROM template.staging_withdraw_data swd WHERE is_locked = 'Yes' AND locked_by = :lockedBy;
    """)
    Flux<String> getSamityIdListLockedByUserForAuthorization(String lockedBy);

    Flux<StagingWithdrawDataEntity> findAllBySamityIdIn(List<String> samityIdList);

    @Query("""
            SELECT * FROM template.staging_withdraw_data WHERE management_process_id = :managementProcessId and case when (:fieldOfficerId is not null and :fieldOfficerId != '') then (created_by = :fieldOfficerId) else 1 = 1 end LIMIT :limit OFFSET :offset;
            """)
    Flux<StagingWithdrawDataEntity> findAllWithdrawStagingDataByLoginId(String managementProcessId, String fieldOfficerId, int limit, int offset);

    @Query("""
            SELECT count(*) FROM template.staging_withdraw_data WHERE management_process_id = :managementProcessId and case when (:fieldOfficerId is not null and :fieldOfficerId != '') then (created_by = :fieldOfficerId) else 1 = 1 end;
            """)
    Mono<Long> countWithdrawData(String managementProcessId, String fieldOfficerId);

    Mono<StagingWithdrawDataEntity> findByOid(String oid);

    Flux<StagingWithdrawDataEntity> findAllByOidIn(List<String> oidList);
}
