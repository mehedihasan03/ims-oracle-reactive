package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataDetailViewEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStagingDataRepository extends ReactiveCrudRepository<StagingDataEntity, String> {
	
	@Query("""
			SELECT
				m.member_id,
				m.member_name_en,
				m.member_name_bn,
				m.mobile,
				m.mfi_id,
				m.*,
			FROM "member" m
			INNER JOIN mem_smt_off_pri_map msopm 
			ON msopm.member_id = m.member_id
			WHERE msopm.samity_id = :samityId
			AND msopm.status = 'Active';
			""")
	Flux<StagingDataEntity> getMemberInfoBySamityId(@Param("samityId") String samityId);
	
	
	@Query("""
			SELECT
			 	sd.staging_data_id,
			 	sd.member_id,
			 	sd.member_name_en,
			 	sd.member_name_bn,
			 	sd.mobile,
			 	sd.mfi_id,
			 	sd.downloaded_on,
			 	sd.downloaded_by
			FROM
			 	staging_data sd
			WHERE
			 	sd.samity_id = :samityId;
			""")
	Flux<StagingDataEntity> getMemberInfoForStagingDataDetailViewBySamityId(@Param("samityId") String samityId);
	
	
	@Query("""
				SELECT DISTINCT
			    sd.samity_id,
			    sd.samity_name_en,
			    sd.samity_name_bn,
			    sd.samity_day,
			    sd.field_officer_id,
			    sd.field_officer_name_en,
			    sd.field_officer_name_bn,
			    sd.mfi_id
			  FROM
			    staging_data sd
			  WHERE
			    sd.samity_id = :samityId;
			""")
	Mono<StagingDataDetailViewEntity> getSamityInfoForStagingDataDetailView(@Param("samityId") String samityId);
	
	
	@Query("""
				SELECT
			    DISTINCT
			    sd.samity_id,
			    sd.samity_name_en,
			    sd.samity_name_bn,
			    sd.samity_day,
			    sd.field_officer_id,
			    sd.field_officer_name_en,
			    sd.field_officer_name_bn,
			    sd.mfi_id
			  FROM
			    staging_data sd
			  INNER JOIN staging_account_data sad ON
			    sad.member_id = sd.member_id
			  WHERE
			    sad.loan_account_id  = :accountId
			    OR sad.savings_account_id = :accountId;
			""")
	Mono<StagingDataDetailViewEntity> getSamityInfoForStagingDataDetailViewByLoanAccountId(@Param("accountId") String accountId);
	
	@Query("""
				SELECT DISTINCT
			    sd.member_id,
			    sd.member_name_en,
			    sd.member_name_bn,
			    sd.mobile,
			    sd.mfi_id,
			    sd.staging_data_id
			  FROM
			    staging_data sd
			  INNER JOIN staging_account_data sad ON
			    sad.member_id = sd.member_id
			  WHERE
			    sad.loan_account_id  = :accountId
			    OR sad.savings_account_id = :accountId;
			""")
	Mono<StagingDataEntity> getMemberInfoForStagingDataDetailViewByAccountId(@Param("accountId") String accountId);
	
	Flux<StagingDataEntity> findAllBySamityId(String samityId);
	
	
	@Query("""
						SELECT
				    DISTINCT
				       sd.samity_id,
					    sd.samity_name_en,
					    sd.samity_name_bn,
					    sd.samity_day,
					    sd.field_officer_id,
					    sd.field_officer_name_en,
					    sd.field_officer_name_bn,
					    sd.mfi_id
				   FROM
				      staging_data sd
				   WHERE
				      sd.member_id = :memberId
				   LIMIT 1;
			""")
	Mono<StagingDataDetailViewEntity> getSamityInfoForStagingDataDetailViewByMemberId(String memberId);
	
/*	@Query("""
				SELECT DISTINCT
			    sd.member_id,
			    sd.member_name_en,
			    sd.member_name_bn,
			    sd.mobile,
			    sd.staging_data_id
			  FROM
			    staging_data sd
				WHERE
					sd.member_id = :memberId;
			""")
	Mono<StagingDataEntity> getMemberInfoForStagingDataDetailViewByMemberId(String memberId);*/

	Mono<StagingDataEntity> getStagingDataEntityByMemberId(String memberId);
	
	Flux<StagingDataEntity> findAllByFieldOfficerId(String fieldOfficerId);
	
	@Query("""
					SELECT
			      DISTINCT sd.samity_id
			    FROM
			      staging_data sd
			    WHERE
			      sd.field_officer_id IN (:fieldOfficerIdList)
			    ORDER BY
			      sd.samity_id
			    LIMIT :limit OFFSET :offset;
			""")
	Flux<String> findSamityIdListByFieldOfficerIdList(List<String> fieldOfficerIdList, Integer limit, Integer offset);
	
	@Query("""
					SELECT
			      count(DISTINCT sd.samity_id)
			    FROM
			      staging_data sd
			    WHERE
			      sd.field_officer_id IN (:fieldOfficerIdList);
			""")
	Mono<Integer> getTotalCountByFieldOfficerList(List<String> fieldOfficerIdList);
	
	@Query("""
					SELECT
			      count(DISTINCT sd.samity_id)
			    FROM
			      staging_data sd
			    WHERE
			      sd.field_officer_id IN (:fieldOfficerIdList)
			      AND sd.samity_day = :samityDay;
			""")
	Mono<Integer> getTotalCountByFieldOfficerListForSamityDay(List<String> fieldOfficerIdList, String samityDay);
	
	@Query("""
					SELECT
			      count(DISTINCT sd.samity_id)
			    FROM
			      staging_data sd
			    WHERE
			      sd.field_officer_id IN (:fieldOfficerIdList)
			      AND sd.samity_day != :samityDay;
			""")
	Mono<Integer> getTotalCountByFieldOfficerListForNonSamityDay(List<String> fieldOfficerIdList, String samityDay);
	
	@Query("""
			    SELECT
			    	DISTINCT
			    	sd.samity_id
			    FROM
			    	staging_data sd
			    WHERE
			    	sd.field_officer_id IN (:fieldOfficerId)
			    	AND sd.samity_day = :samityDay
			    LIMIT :limit OFFSET :offset;
			""")
	Flux<String> findSamityIdListByFieldOfficerIdListForSamityDay(List<String> fieldOfficerIdList, String samityDay, Integer limit, Integer offset);
	
	@Query("""
			    SELECT
			    	DISTINCT
			    	sd.samity_id
			    FROM
			    	staging_data sd
			    WHERE
			    	sd.field_officer_id IN (:fieldOfficerId)
			    	AND sd.samity_day != :samityDay
			    LIMIT :limit OFFSET :offset;
			""")
	Flux<String> findSamityIdListByFieldOfficerIdListForNonSamityDay(List<String> fieldOfficerIdList, String samityDay, Integer limit, Integer offset);
	
	@Query("""
				SELECT DISTINCT s.samity_id FROM samity s WHERE s.field_officer_id = :fieldOfficerId ORDER BY s.samity_id;
			""")
	Flux<String> getSamityIdListByFieldOfficer(String fieldOfficerId);


	Mono<Void> deleteAllByProcessId(String processId);

	Flux<StagingDataEntity> findAllByManagementProcessIdOrderBySamityId(String managementProcessId);

	Mono<Void> deleteAllByManagementProcessId(String managementProcessId);

	Flux<StagingDataEntity> findAllByProcessIdAndSamityId(String processId, String SamityId);

	Mono<Void> deleteAllByProcessIdAndSamityId(String processId, String samityId);

	Mono<StagingDataEntity> findFirstByMemberId(String memberId);

	Flux<StagingDataEntity> findAllByManagementProcessIdAndProcessIdAndSamityIdIn(String managementProcessId, String processId, List<String> samityIdList);

	Flux<StagingDataEntity> findAllByManagementProcessIdAndSamityId(String managementProcessId, String samityId);
	Flux<StagingDataEntity> findAllBySamityIdIn(List<String> samityIDList);

	Mono<StagingDataEntity> findFirstByStagingDataId(String stagingDataId);
}
