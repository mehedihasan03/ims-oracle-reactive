package net.celloscope.mraims.loanportfolio.features.migration.components.office;

import net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata.DeleteDataByManagementProcessIdRepository;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeleteOfficeEventTrackerRepository extends ReactiveCrudRepository<OfficeEventTrackerEntity, String>, DeleteDataByManagementProcessIdRepository<OfficeEventTrackerEntity, String> {
    Mono<Void> deleteAllByManagementProcessIdAndOfficeEventNot(String managementProcessId, String officeEvent);
}
