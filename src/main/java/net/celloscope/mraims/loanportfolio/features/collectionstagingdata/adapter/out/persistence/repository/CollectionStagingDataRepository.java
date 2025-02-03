package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CollectionStagingDataRepository extends ReactiveCrudRepository<CollectionStagingDataEntity, String> {
    Flux<CollectionStagingDataEntity> findByStagingDataId(String stagingDataId);

    Mono<Long> countAllByLoanAccountIdIn(List<String> loanAccountsList);

    Mono<Long> countAllBySavingsAccountIdIn(List<String> savingsAccountsList);

    Flux<CollectionStagingDataEntity> findAllBySamityIdAndCollectionTypeAndStatus(String samityId, String collectionType, String status);

    Mono<CollectionStagingDataEntity> findCollectionStagingDataEntityByLoanAccountId(String loanAccountId);

    Mono<CollectionStagingDataEntity> findCollectionStagingDataEntityBySavingsAccountId(String savingsAccountId);

    Flux<CollectionStagingDataEntity> findAllBySamityIdAndCollectionType(String samityId, String collectionType);

    @Query("""
                SELECT DISTINCT samity_day FROM staging_data sd WHERE sd.samity_id = :samityId LIMIT 1;
            """)
    Mono<String> getSamityDayFromSamityId(String samityId);

    @Query("""
                SELECT
                	count(*)
                FROM
                	staging_data sd
                INNER JOIN staging_account_data sad ON
                	sd.member_id = sad.member_id
                WHERE
                	sd.samity_id = :samityId
                	AND
                	sd.staging_data_id IN (:stagingDataIdList)
                	AND
                	sad.loan_account_id IN (:loanAccountIdList);
            """)
    Mono<Integer> getCollectionDataCountForLoanAccountToVerifyPayment(String samityId, List<String> stagingDataIdList, List<String> loanAccountIdList);

    @Query("""
                SELECT
                	count(*)
                FROM
                	staging_data sd
                INNER JOIN staging_account_data sad ON
                	sd.member_id = sad.member_id
                WHERE
                	sd.samity_id = :samityId
                	AND
                	sd.staging_data_id IN (:stagingDataIdList)
                	AND
                	sad.savings_account_id IN (:savingsAccountIdList);
            """)
    Mono<Integer> getCollectionDataCountForSavingsAccountToVerifyPayment(String samityId, List<String> stagingDataIdList, List<String> savingsAccountIdList);

    @Query("""
                SELECT
                	sum(amount)
                FROM
                	collection_staging_data csd
                WHERE
                	csd.samity_id = :samityId;
            """)
    Mono<BigDecimal> getTotalCollectionBySamityId(String samityId);

    Flux<CollectionStagingDataEntity> findAllBySamityId(String samityId);

    @Query("""
                SELECT count(csd.samity_id) FROM collection_staging_data csd WHERE csd.samity_id = :samityId;
            """)
    Mono<Integer> getCountOfCollectionDataBySamityId(String samityId);

    @Query(("""
                SELECT csd.staging_data_id FROM collection_staging_data csd WHERE csd.samity_id = :samityId;
            """))
    Flux<String> getStagingDataIdListBySamity(String samityId);


    Flux<CollectionStagingDataEntity> findAllByManagementProcessIdOrderBySamityId(String managementProcessId);

    Flux<CollectionStagingDataEntity> findAllByManagementProcessIdAndProcessIdOrderBySamityId(String managementProcessId, String processId);

    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);

    Flux<CollectionStagingDataEntity> findAllBySamityIdOrderByCreatedOn(String samityId);

    Mono<CollectionStagingDataEntity> findDistinctFirstBySamityId(String samityId);

    @Query("""
        SELECT DISTINCT samity_id FROM collection_staging_data csd WHERE is_locked = 'Yes' AND locked_by = :lockedBy;
    """)
    Flux<String> getSamityIdListLockedByUserForAuthorization(String lockedBy);

    Flux<CollectionStagingDataEntity> findAllBySamityIdIn(List<String> samityIdList);

    Flux<CollectionStagingDataEntity> findAllByManagementProcessIdAndSamityId(String managementProcessId, String SamityId);

    Mono<CollectionStagingDataEntity> findFirstByLoanAccountIdAndManagementProcessIdAndProcessIdOrderByCreatedOnDesc(String loanAccountId, String managementProcessId, String processId);

    Mono<Boolean> deleteAllByManagementProcessIdAndProcessId(String managementProcessId, String processId);

    @Query("""
            SELECT * FROM collection_staging_data WHERE management_process_id = :managementProcessId and collection_type = :collectionType  AND case when (:fieldOfficerId is not null and :fieldOfficerId != '') then (created_by = :fieldOfficerId) else 1 = 1 end LIMIT :limit OFFSET :offset;
            """)
    Flux<CollectionStagingDataEntity> findCollectionStagingDataByCollectionType(String managementProcessId, String collectionType, String fieldOfficerId, int limit, int offset);

    @Query("""
            SELECT count(*) FROM collection_staging_data WHERE management_process_id = :managementProcessId and collection_type = :collectionType  AND case when (:fieldOfficerId is not null and :fieldOfficerId != '') then (created_by = :fieldOfficerId) else 1 = 1 end;
            """)
    Mono<Long> countStagingCollectionData(String managementProcessId, String collectionType, String fieldOfficerId);

    Mono<CollectionStagingDataEntity> findByOid(String oid);
    Mono<Void> deleteByOid(String oid);

    Flux<CollectionStagingDataEntity> findAllByOidIn(List<String> oidList);

    @Modifying
    @Query("""
        UPDATE collection_staging_data 
        SET 
            created_by = CASE 
                WHEN amount = 0 AND :newAmount > 0 THEN :updatedBy 
                ELSE created_by 
            END,
            created_on = CASE
                WHEN amount = 0 AND :newAmount > 0 THEN CURRENT_TIMESTAMP 
                ELSE created_on
            END,
            updated_by = CASE 
                WHEN amount != 0 AND amount != :newAmount THEN :updatedBy 
                WHEN amount = 0 AND :newAmount > 0 THEN :updatedBy
                ELSE updated_by 
            END,
            updated_on = CASE
                WHEN amount != 0 AND amount != :newAmount THEN CURRENT_TIMESTAMP 
                WHEN amount = 0 AND :newAmount > 0 THEN CURRENT_TIMESTAMP 
                ELSE updated_on
            END,
            amount = :newAmount
        WHERE oid = :oid
    """)
    Mono<Integer> conditionalUpdate(String oid, BigDecimal newAmount, String updatedBy);


    Mono<Long> countByManagementProcessIdAndSamityId(String managementProcessId, String samityId);
}
