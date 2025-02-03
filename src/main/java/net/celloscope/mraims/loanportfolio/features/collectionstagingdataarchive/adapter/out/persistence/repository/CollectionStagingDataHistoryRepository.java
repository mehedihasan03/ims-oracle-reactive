package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity.CollectionStagingDataHistoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CollectionStagingDataHistoryRepository extends R2dbcRepository<CollectionStagingDataHistoryEntity, String> {

    @Query("""
            select csdh.*
                from collection_staging_data_history csdh
                    join collection_staging_data csd
                        on csdh.collection_staging_data_id = csd.collection_staging_data_id
                    join mem_smt_off_pri_map msopm
                        on csd.samity_id = msopm.samity_id
                where msopm.office_id  = :OFFICE_ID
                    and csd.status = :STATUS
                    and msopm.status = 'Active';
            """)
    Flux<CollectionStagingDataHistoryEntity> getByOfficeId(@Param("OFFICE_ID") String officeId, @Param("STATUS") String status);


    Mono<CollectionStagingDataHistoryEntity> findFirstByLoanAccountIdAndManagementProcessIdAndProcessIdOrderByCreatedOnDesc(String loanAccountId, String managementProcessId, String processId);
}
