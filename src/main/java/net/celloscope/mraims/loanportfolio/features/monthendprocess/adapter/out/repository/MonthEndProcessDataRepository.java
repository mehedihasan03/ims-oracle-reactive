package net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessDataEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.domain.MonthEndProcessData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MonthEndProcessDataRepository extends ReactiveCrudRepository<MonthEndProcessDataEntity, String> {
    Flux<MonthEndProcessDataEntity> findAllByManagementProcessIdAndOfficeIdOrderBySamityId(String managementProcessId, String officeId);
    Mono<MonthEndProcessDataEntity> findFirstByManagementProcessIdAndSamityId(String managementProcessId, String samityId);
    Mono<MonthEndProcessDataEntity> findFirstByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
    Flux<MonthEndProcessDataEntity> findAllByManagementProcessId(String managementProcessId);
}
