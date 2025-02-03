package net.celloscope.mraims.loanportfolio.features.migrationV3.components.transaction;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DeleteTransactionRepositoryV3 extends R2dbcRepository<TransactionEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<TransactionEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
