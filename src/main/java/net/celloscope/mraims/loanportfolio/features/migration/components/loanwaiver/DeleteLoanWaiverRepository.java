package net.celloscope.mraims.loanportfolio.features.migration.components.loanwaiver;

import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeleteLoanWaiverRepository extends ReactiveCrudRepository<LoanWaiverEntity, String>, DeleteDataByManagementProcessIdRepository<LoanWaiverEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
