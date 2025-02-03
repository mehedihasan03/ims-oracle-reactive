package net.celloscope.mraims.loanportfolio.features.migrationV3.components.passbook;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity.PassbookEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DeletePassbookRepositoryV3 extends R2dbcRepository<PassbookEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<PassbookEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
