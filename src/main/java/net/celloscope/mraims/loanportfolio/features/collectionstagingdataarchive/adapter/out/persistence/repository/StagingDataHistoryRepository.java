package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity.StagingDataHistoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface StagingDataHistoryRepository extends R2dbcRepository<StagingDataHistoryEntity, String> {

    @Query("""
            select sdh.*
                from staging_data_history sdh
                    join staging_data sd
                        on sdh.staging_data_id = sd.staging_data_id
                    join mem_smt_off_pri_map msopm
                        on sd.member_id = msopm.member_id
                where msopm.office_id = :OFFICE_ID
                and msopm.status = 'Active';
            """)
    Flux<StagingDataHistoryEntity> getByOfficeId(@Param("OFFICE_ID") String officeId);
}
