package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.entity.StagingAccountDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StagingAccountDataArchiveRepository extends R2dbcRepository<StagingAccountDataEntity, String> {

    @Query("""
            select sad.*
            from template.staging_account_data sad
            join template.mem_smt_off_pri_map msopm
            on sad.member_id = msopm.member_id
            where msopm.office_id = :OFFICE_ID
            and msopm.status = 'Active';
            """)
    Flux<StagingAccountDataEntity> getByOfficeId(@Param("OFFICE_ID") String officeId);

    Mono<Void> deleteByStagingAccountDataId(String id);
}
