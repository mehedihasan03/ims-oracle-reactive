package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RepaymentScheduleEditHistoryPersistencePort {

    Mono<List<RepaymentSchedule>> saveRepaymentScheduleEditHistory(List<RepaymentSchedule> repaymentSchedule);

    Flux<RepaymentSchedule> getAllRepaymentScheduleEditHistoryByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId);

    Mono<Void> deleteAllRepaymentEditHistoryByLoanRepayOid(List<String> oidList);
}
