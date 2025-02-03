package net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AutoVoucherPersistenceRepository extends ReactiveCrudRepository<AutoVoucherEntity, String> {
    Flux<AutoVoucherEntity> findAllByManagementProcessIdAndProcessId(String managementProcessId, String processId);
    Mono<Boolean> deleteAllByManagementProcessIdAndProcessId(String managementProcessId, String processId);
    Mono<AutoVoucherEntity> findByOid(String oid);
    Flux<AutoVoucherEntity> findAllByManagementProcessId(String managementProcessId);
    Mono<Boolean> deleteAllByManagementProcessId(String managementProcessId);
}
