package net.celloscope.mraims.loanportfolio.features.migrationV3.components.autovoucher;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherEntity;
import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteAutoVoucherPersistenceRepositoryV3 extends ReactiveCrudRepository<AutoVoucherEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<AutoVoucherEntity, String> {
    Mono<Void> deleteAllByOfficeId(String officeId);
}
