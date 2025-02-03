package net.celloscope.mraims.loanportfolio.features.migrationV3.components.autovoucher;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteAutoVoucherDetailPersistenceRepositoryV3 extends ReactiveCrudRepository<DeleteAutoVoucherDetailEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<DeleteAutoVoucherDetailEntity,String> {
    Mono<Void> deleteAllByOfficeId(String officeId);
}
