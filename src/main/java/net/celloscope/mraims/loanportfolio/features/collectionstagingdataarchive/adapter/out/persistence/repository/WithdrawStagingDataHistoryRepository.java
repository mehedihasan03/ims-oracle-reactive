package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity.StagingDataHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity.WithdrawStagingDataHistoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface WithdrawStagingDataHistoryRepository extends R2dbcRepository<WithdrawStagingDataHistoryEntity, String> {

    @Query("""
            select sdh.*
            from staging_withdraw_data_history sdh
            join staging_withdraw_data swd
            on sdh.withdraw_staging_data_id = swd.withdraw_staging_data_id
            join staging_data sd
            on sd.staging_data_id = swd.staging_data_id
            join mem_smt_off_pri_map msopm
            on sd.member_id = msopm.member_id
            where msopm.office_id = :OFFICE_ID
            and msopm.status = 'Active';
            """)
    Flux<WithdrawStagingDataHistoryEntity> getByOfficeId(@Param("OFFICE_ID") String officeId);
}
