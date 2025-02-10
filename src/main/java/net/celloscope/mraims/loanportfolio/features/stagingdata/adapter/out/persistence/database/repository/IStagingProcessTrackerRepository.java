package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataGenerationStatusEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.SamityMemberCount;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IStagingProcessTrackerRepository extends ReactiveCrudRepository<StagingProcessTrackerEntity, String> {

    @Query("""
           SELECT DISTINCT
            s.samity_id,
            s.samity_name_en,
            s.samity_name_bn,
            s.samity_day,
            s.field_officer_id,
            e.emp_name_en AS field_officer_name_en,
            e.emp_name_bn AS field_officer_name_bn,
            msopm.office_id
           FROM
            template.samity s
           INNER JOIN template.mem_smt_off_pri_map msopm ON
            s.samity_id = msopm.samity_id
           INNER JOIN template.employee e ON
            s.field_officer_id = e.employee_id
           WHERE
            msopm.office_id = :officeId
            AND
            msopm.status = 'Active'; 
           """)
    Flux<StagingDataGenerationStatusEntity> getStagingDataGenerationStatusByOfficeId(@Param("officeId") String officeId);

    @Query("""
            SELECT
                DISTINCT sd.total_member
            FROM
                template.staging_data sd
            WHERE
                sd.samity_id = :samityId
            LIMIT 1;
            """)
    Mono<Integer> getTotalMemberForOneSamity(String samityId);

    Mono<StagingProcessTrackerEntity> getStagingProcessTrackerEntityBySamityId(String samityId);

    @Query("""
            SELECT DISTINCT
                s.samity_id,
                s.samity_name_en,
                s.samity_name_bn,
                s.samity_day,
                s.field_officer_id,
                e.emp_name_en AS field_officer_name_en,
                e.emp_name_bn AS field_officer_name_bn,
                msopm.office_id,
                spt.process_id,
                spt.status,
                spt.process_start_time,
                spt.process_end_time
            FROM
                template.staging_process_tracker spt
            INNER JOIN template.samity s ON
                s.samity_id = spt.samity_id
            INNER JOIN template.mem_smt_off_pri_map msopm ON
                s.samity_id = msopm.samity_id
            INNER JOIN template.employee e ON
                s.field_officer_id = e.employee_id
            WHERE
                msopm.office_id = :officeId
            AND
                msopm.status = 'Active';
            """)
    Flux<StagingDataGenerationStatusEntity> getStagingDataGenerationStatusResponse(String officeId);

    @Query("""
            	SELECT DISTINCT
            		sd.staging_data_id
            	FROM
            		template.staging_data sd
            	WHERE
            		sd.samity_id = :samityId
	            AND sd.management_process_id = :managementProcessId;
            """)
    Flux<String> getStagingDataIdBySamity(String samityId, String managementProcessId);

    Mono<StagingProcessTrackerEntity> findStagingProcessTrackerEntityBySamityId(String samityId);

    @Query("""
    SELECT DISTINCT
    count(msopm.member_id)
    FROM 
    template.mem_smt_off_pri_map msopm
    WHERE
    msopm.samity_id = :samityId
    AND 
    msopm.status = 'Active';
    """)
    Mono<Integer> getTotalMemberOfSamityToGenerateStagingData(String samityId);

    @Query("""
            SELECT
                msopm.samity_id,
                count(*)
            FROM
                template.mem_smt_off_pri_map msopm
            WHERE
                msopm.office_id = :officeId
            AND 
                msopm.status = 'Active'
            GROUP BY
                1;
            """)
    Flux<SamityMemberCount> getTotalMemberListOfSamityByOfficeId(String officeId);

    Mono<Void> deleteAllByProcessId(String processId);

    @Query("""
            SELECT DISTINCT
                spt.samity_id
            FROM
                template.mem_smt_off_pri_map msopm
            INNER JOIN 
                template.staging_process_tracker spt 
            ON msopm.samity_id = spt.samity_id
            WHERE
                msopm.office_id = :officeId
            AND
                msopm.status = 'Active';
            """)
    Flux<String> getAllSamityIdListByOfficeId(String officeId);

    Flux<StagingProcessTrackerEntity> findAllByManagementProcessIdOrderBySamityId(String managementProcessId);

    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);

//    Process Management v2
    Flux<StagingProcessTrackerEntity> findAllBySamityIdInOrderBySamityId(List<String> samityIdList);

    Mono<StagingProcessTrackerEntity> findFirstByProcessIdAndSamityId(String processId, String samityId);

    Flux<StagingProcessTrackerEntity> findAllByManagementProcessIdAndOfficeIdOrderBySamityId(String managementProcessId, String officeId);
    Flux<StagingProcessTrackerEntity> findAllByManagementProcessIdAndFieldOfficerIdOrderBySamityId(String managementProcessId, String fieldOfficerId);
    Flux<StagingProcessTrackerEntity> findAllByManagementProcessIdAndSamityIdInOrderBySamityId(String managementProcessId, List<String> samityIdList);

    Mono<StagingProcessTrackerEntity> findFirstByManagementProcessIdAndSamityId(String managementProcessId, String samityId);

    Flux<StagingProcessTrackerEntity> findAllByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);

}
