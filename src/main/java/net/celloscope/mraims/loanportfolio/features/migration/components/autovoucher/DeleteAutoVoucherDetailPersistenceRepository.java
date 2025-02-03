package net.celloscope.mraims.loanportfolio.features.migration.components.autovoucher;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherDetailEntity;
import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeleteAutoVoucherDetailPersistenceRepository extends ReactiveCrudRepository<DeleteAutoVoucherDetailEntity, String>, DeleteDataByManagementProcessIdRepository<DeleteAutoVoucherDetailEntity,String> {
    Mono<Void> deleteAllByOfficeId(String officeId);
}
