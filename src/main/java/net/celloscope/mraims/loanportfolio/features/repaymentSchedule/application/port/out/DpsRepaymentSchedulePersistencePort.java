package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.DPSRepaymentScheduleEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.DpsRepaymentSchedule;
import org.modelmapper.ModelMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

public interface DpsRepaymentSchedulePersistencePort {
    Mono<List<DpsRepaymentSchedule>> saveRepaymentSchedule(List<DpsRepaymentSchedule> dpsRepaymentScheduleList);

    Flux<DpsRepaymentSchedule> getDPSRepaymentScheduleBySavingsAccountId(String savingsAccountId);

    Mono<Boolean> updateDPSRepaymentScheduleStatus(String savingsAccountId, String status, List<Integer> paidRepaymentNos, String managementProcessId, LocalDate businessDate, String loginId);
    Mono<Integer> countPendingDpsRepaymentScheduleBySavingsAccountId(String savingsAccountId);

    Flux<DPSRepaymentScheduleEntity> updateDPSRepaymentScheduleStatusByManagementProcessId(String managementProcessId, String status, String loginId);
}
