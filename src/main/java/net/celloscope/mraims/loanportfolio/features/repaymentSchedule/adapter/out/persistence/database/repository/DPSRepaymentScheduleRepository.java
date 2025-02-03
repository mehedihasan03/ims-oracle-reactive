package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.DPSRepaymentScheduleEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface DPSRepaymentScheduleRepository extends ReactiveCrudRepository<DPSRepaymentScheduleEntity, String> {
    Flux<DPSRepaymentScheduleEntity> getDPSRepaymentScheduleEntitiesBySavingsAccountId(String savingsAccountId);
    @Query("""
    UPDATE dps_repayment_schedule drs
    SET status = :status , updated_on = :businessDate, updated_by = :loginId, management_process_id = :managementProcessId, actual_repayment_date = :businessDate
    WHERE savings_account_id = :savingsAccountId
    AND repayment_no IN (:paidRepaymentNos);
    """)
    Mono<Boolean> updateDpsRepaymentScheduleStatus(String savingsAccountId, String status, List<Integer> paidRepaymentNos, String managementProcessId, LocalDate businessDate, String loginId);

    Mono<Integer> countDPSRepaymentScheduleEntitiesBySavingsAccountIdAndStatus(String savingsAccountId, String status);

    Flux<DPSRepaymentScheduleEntity> getAllByManagementProcessId(String managementProcessId);
}
