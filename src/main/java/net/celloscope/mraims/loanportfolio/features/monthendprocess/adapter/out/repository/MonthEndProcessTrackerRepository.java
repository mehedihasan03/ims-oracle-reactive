package net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.repository;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity.MonthEndProcessTrackerEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MonthEndProcessTrackerRepository extends ReactiveCrudRepository<MonthEndProcessTrackerEntity, String> {

    @Query("""
        SELECT * FROM template.month_end_process_tracker mept WHERE office_id = :officeId ORDER BY month_end_date DESC LIMIT :limit OFFSET :offset;
    """)
    Flux<MonthEndProcessTrackerEntity> findAllByOfficeIdOrderByMonthEndDateDesc(String officeId, Integer limit, Integer offset);

    Flux<MonthEndProcessTrackerEntity> findAllByManagementProcessIdAndOfficeId(String managementProcessId, String officeId);

    Flux<MonthEndProcessTrackerEntity> findAllByManagementProcessId(String managementProcessId);

    Mono<MonthEndProcessTrackerEntity> findFirstByManagementProcessIdAndOfficeIdAndTransactionCode(String managementProcessId, String officeId, String transactionCode);

}
