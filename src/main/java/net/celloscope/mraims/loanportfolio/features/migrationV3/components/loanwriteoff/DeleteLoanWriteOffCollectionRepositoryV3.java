package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanwriteoff;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DeleteLoanWriteOffCollectionRepositoryV3 extends R2dbcRepository<LoanWriteOffCollectionEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<LoanWriteOffCollectionEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
