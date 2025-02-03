package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity.StagingAccountDataHistoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StagingAccountDataHistoryRepository extends R2dbcRepository<StagingAccountDataHistoryEntity, String> {

    @Query("""
            select sadh.*
                from staging_account_data_history sadh
                    join staging_account_data sad
                        on sadh.staging_account_data_id = sad.staging_account_data_id
                    join mem_smt_off_pri_map msopm
                        on sad.member_id = msopm.member_id
                where msopm.office_id = :OFFICE_ID
                and msopm.status = 'Active';
            """)
    Flux<StagingAccountDataHistoryEntity> getByOfficeId(@Param("OFFICE_ID") String officeId);

    Mono<StagingAccountDataHistoryEntity> findByLoanAccountIdAndManagementProcessId(String loanAccountId, String managementProcessId);

}
