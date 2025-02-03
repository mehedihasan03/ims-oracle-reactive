package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RepaymentScheduleEditHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RepaymentScheduleEditHistoryRepository extends ReactiveCrudRepository<RepaymentScheduleEditHistoryEntity, String> {

    Mono<Boolean> deleteRepaymentScheduleEditHistoryEntitiesByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId);

    Flux<RepaymentScheduleEditHistoryEntity> findAllByManagementProcessIdAndLoanAccountId(String managementProcessId, String loanAccountId);

    Mono<Void> deleteAllByLoanRepayScheduleOidIn(List<String> loanRepayOid);
}
