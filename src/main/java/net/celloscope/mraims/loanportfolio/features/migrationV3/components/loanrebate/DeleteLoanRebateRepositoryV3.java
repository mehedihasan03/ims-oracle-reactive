package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanrebate;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DeleteLoanRebateRepositoryV3 extends R2dbcRepository<LoanRebateEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<LoanRebateEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
