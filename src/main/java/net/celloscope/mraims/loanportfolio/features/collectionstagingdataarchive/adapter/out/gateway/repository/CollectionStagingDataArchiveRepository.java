package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CollectionStagingDataArchiveRepository extends R2dbcRepository<CollectionStagingDataEntity, String> {

    @Query("""
            select csd.*
            from collection_staging_data csd
            join mem_smt_off_pri_map msopm
            on csd.samity_id = msopm.samity_id
            where msopm.office_id  = :OFFICE_ID
            and msopm.status = 'Active';
            """)
    Flux<CollectionStagingDataEntity> getByOfficeId(@Param("OFFICE_ID") String officeId);

    Mono<Void> deleteByCollectionStagingDataId(String id);
}
