package net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity.DayEndProcessTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DayEndProcessTrackerRepository extends ReactiveCrudRepository<DayEndProcessTrackerEntity, String> {

    Flux<DayEndProcessTrackerEntity> findAllByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);
    Flux<DayEndProcessTrackerEntity> findAllByManagementProcessId(String managementProcessId);
    Mono<DayEndProcessTrackerEntity> findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(String managementProcessId, String officeId, String transactionCode);
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
