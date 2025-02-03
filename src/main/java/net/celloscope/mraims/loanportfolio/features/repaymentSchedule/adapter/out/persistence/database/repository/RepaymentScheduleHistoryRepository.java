package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RepaymentScheduleHistoryEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RepaymentScheduleHistoryRepository extends ReactiveCrudRepository<RepaymentScheduleHistoryEntity, String> {
    Flux<RepaymentScheduleHistoryEntity> findAllByManagementProcessId(String managementProcessId);
    Mono<Void> deleteAllByLoanRepayScheduleOidIn(List<String> loanRepayOid);

    @Query("""
            SELECT * FROM loan_repayment_schedule_history lrs WHERE lrs.management_process_id = :managementProcessId;
            """)
    Flux<RepaymentScheduleHistoryEntity> findAllByManagementProcessIdV2(String managementProcessId);
    Flux<RepaymentScheduleHistoryEntity> findAllByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId);
}
