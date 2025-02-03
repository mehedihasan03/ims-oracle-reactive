package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.entity.StagingDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StagingDataArchiveRepository extends R2dbcRepository<StagingDataEntity, String> {

    @Query("""
            select sd.*
            from staging_data sd
            join mem_smt_off_pri_map msopm
            on sd.member_id = msopm.member_id
            where msopm.office_id = :OFFICE_ID
            and msopm.status = 'Active';
            """)
    Flux<StagingDataEntity> getByOfficeId(@Param("OFFICE_ID") String officeId);

    Mono<Void> deleteByStagingDataId(String id);
}
