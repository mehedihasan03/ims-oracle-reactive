package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RepaymentScheduleHistoryPersistencePort {
    Mono<List<RepaymentSchedule>> saveRepaymentScheduleHistory(List<RepaymentSchedule> repaymentSchedule);
    Flux<RepaymentSchedule> getAllRepaymentScheduleHistoryByManagementProcessId(String managementProcessId);
    Mono<Void> deleteAllRepaymentHistoryByLoanRepayOid(List<String> oidList);
    Flux<RepaymentSchedule> getAllRepaymentScheduleHistoryByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId);
}
