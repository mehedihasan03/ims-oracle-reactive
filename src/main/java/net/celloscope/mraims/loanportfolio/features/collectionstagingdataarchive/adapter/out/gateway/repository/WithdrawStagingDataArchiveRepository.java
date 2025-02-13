package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.entity.WithdrawStagingDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WithdrawStagingDataArchiveRepository extends R2dbcRepository<WithdrawStagingDataEntity, String> {

    @Query("""
            select swd.*
                from template.staging_withdraw_data swd
                    join template.staging_data sd
                        on sd.staging_data_id = swd.staging_data_id
                    join template.mem_smt_off_pri_map msopm
                        on sd.member_id = msopm.member_id
                where msopm.office_id = :OFFICE_ID
                and msopm.status = 'Active';
            """)
    Flux<WithdrawStagingDataEntity> getByOfficeId(@Param("OFFICE_ID") String officeId);

    Mono<Void> deleteByWithdrawStagingDataId(String id);
}
