package net.celloscope.mraims.loanportfolio.features.migration.components.autovoucher;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherEntity;
import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeleteAutoVoucherPersistenceRepository extends ReactiveCrudRepository<AutoVoucherEntity, String>, DeleteDataByManagementProcessIdRepository<AutoVoucherEntity, String> {
    Mono<Void> deleteAllByOfficeId(String officeId);
}
