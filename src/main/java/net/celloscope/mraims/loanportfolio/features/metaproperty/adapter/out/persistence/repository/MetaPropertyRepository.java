package net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.out.persistence.repository;


import net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.out.persistence.entity.MetaPropertyEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MetaPropertyRepository extends R2dbcRepository<MetaPropertyEntity, String> {


    @Query("""
            select "oid", property_id, property_value, description, status
            from template.metaproperty
            where description = :DESC
            and status = :STATUS;
            """)
    Mono<MetaPropertyEntity> findMetaPropertyByDescriptionAndStatus(@Param("DESC") String description, @Param("STATUS") String status);


    Mono<MetaPropertyEntity> findMetaPropertyEntityByPropertyIdAndStatus(String propertyId, String status);
}
