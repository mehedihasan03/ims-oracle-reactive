package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.entity.StagingDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StagingDataRepository extends R2dbcRepository<StagingDataEntity, String> {
    Flux<StagingDataEntity> findAllByFieldOfficerId(String fieldOfficerId);

    Flux<StagingDataEntity> findAllBySamityId(String samityId);

    Flux<StagingDataEntity> findAllByFieldOfficerIdAndSamityDay(String fieldOfficerId, String samityDay);

    Flux<StagingDataEntity> findAllByMfiId(String mfiId);

    @Query("""
                SELECT
                	*
                FROM
                	staging_data sd
                WHERE
                	sd.field_officer_id = :fieldOfficerId
                	AND sd.samity_day != :samityDay;
            """)
    Flux<StagingDataEntity> findAllByFieldOfficerIdForNonSamityDay(String fieldOfficerId, String samityDay);

    @Query("""
                SELECT
                	DISTINCT sd.field_officer_id,
                	sd.field_officer_name_en,
                	sd.field_officer_name_bn
                FROM
                	staging_data sd
                WHERE
                	sd.field_officer_id = :fieldOfficerId
                LIMIT 1;
            """)
    Mono<StagingDataEntity> getEmployeeDetailByEmployeeId(String fieldOfficerId);

    Flux<StagingDataEntity> findAllBySamityIdAndSamityDay(String samityId, String samityDay);

    Mono<StagingDataEntity> findFirstBySamityId(String samityId);
}
