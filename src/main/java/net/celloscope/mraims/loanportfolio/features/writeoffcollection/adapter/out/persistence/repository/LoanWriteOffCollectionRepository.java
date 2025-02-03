package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.out.persistence.entity.LoanWriteOffCollectionEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

public interface LoanWriteOffCollectionRepository extends ReactiveCrudRepository<LoanWriteOffCollectionEntity, String> {

    @Query("""
            select * from loan_write_off_collection lwoc where lwoc.samity_id like :officeId || '%' and lwoc.created_on between :startDate and :endDate;
            """)
    Flux<LoanWriteOffCollectionEntity> getLoaWriteOffDataByOfficeIdInASpecificDateRange(String officeId, LocalDateTime startDate, LocalDateTime endDate);

    Mono<LoanWriteOffCollectionEntity> findWriteOffCollectionDetailsInfoByOid(String oid);
    Flux<LoanWriteOffCollectionEntity> findAllByManagementProcessId(String managementProcessId);
    Flux<LoanWriteOffCollectionEntity> findAllBySamityIdIn(List<String> samityIdList);

    @Query("""
                SELECT DISTINCT samity_id FROM loan_write_off_collection WHERE is_locked = 'Yes' AND locked_by = :lockedBy;
            """)
    Flux<String> getSamityIdListLockedByUserForAuthorization(String loginId);
    Flux<LoanWriteOffCollectionEntity> findAllByManagementProcessIdAndSamityId(String managementProcessId, String samityId);

    @Query("""
    update loan_write_off_collection
    set approved_on = :approvedOn, approved_by = :loginId, status = :status, locked_by = null, locked_on = null, is_locked = 'No', edit_commit = 'No'
    where oid = :oid; """)
    Flux<LoanWriteOffCollectionEntity> updateLoanWriteOffEntitiesForAuthorization(String oid, String loginId, LocalDateTime approvedOn, String status);
    Mono<LoanWriteOffCollectionEntity> findByLoanWriteOffCollectionDataId(String loanWriteOffCollectionDataId);
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
    Mono<LoanWriteOffCollectionEntity> findByLoanAccountId(String loanAccountId);
    Mono<Void> deleteByOid(String oid);
}
