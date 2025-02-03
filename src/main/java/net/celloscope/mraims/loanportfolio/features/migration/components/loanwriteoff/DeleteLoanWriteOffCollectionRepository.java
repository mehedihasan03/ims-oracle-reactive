package net.celloscope.mraims.loanportfolio.features.migration.components.loanwriteoff;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DeleteLoanWriteOffCollectionRepository extends R2dbcRepository<LoanWriteOffCollectionEntity, String>, DeleteDataByManagementProcessIdRepository<LoanWriteOffCollectionEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
