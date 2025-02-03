package net.celloscope.mraims.loanportfolio.features.migration.components.loanrebate;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity.LoanRebateEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface DeleteLoanRebateRepository extends R2dbcRepository<LoanRebateEntity, String>, DeleteDataByManagementProcessIdRepository<LoanRebateEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
