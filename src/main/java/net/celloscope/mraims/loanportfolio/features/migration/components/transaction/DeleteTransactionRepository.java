package net.celloscope.mraims.loanportfolio.features.migration.components.transaction;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import net.celloscope.mraims.loanportfolio.features.transaction.adapter.out.persistence.database.entity.TransactionEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeleteTransactionRepository extends R2dbcRepository<TransactionEntity, String>, DeleteDataByManagementProcessIdRepository<TransactionEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
