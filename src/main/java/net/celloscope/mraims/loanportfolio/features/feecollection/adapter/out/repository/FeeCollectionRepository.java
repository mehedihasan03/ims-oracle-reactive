package net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out.entity.FeeCollectionEntity;
import org.modelmapper.ModelMapper;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface FeeCollectionRepository extends ReactiveCrudRepository<FeeCollectionEntity, String> {

    Flux<FeeCollectionEntity> findAllByOfficeIdAndManagementProcessId(String officeId, String managementProcessId);
    Flux<FeeCollectionEntity> findAllByOfficeIdAndManagementProcessIdOrManagementProcessIdIsNull(String officeId, String managementProcessId);
    @Query("""
    update fee_collection
    set management_process_id = null, credit_ledger_id = null , credit_subledger_id = null, status = :status
    where management_process_id = :managementProcessId and office_id = :officeId;    
    """)
    Flux<FeeCollectionEntity> rollbackFeeCollectionOnMISDayEndRevert(String officeId, String managementProcessId, String status);
}
