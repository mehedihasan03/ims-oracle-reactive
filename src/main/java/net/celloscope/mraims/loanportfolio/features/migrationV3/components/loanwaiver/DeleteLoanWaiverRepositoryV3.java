package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanwaiver;

import net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.out.entity.LoanWaiverEntity;
import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteLoanWaiverRepositoryV3 extends ReactiveCrudRepository<LoanWaiverEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<LoanWaiverEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
