package net.celloscope.mraims.loanportfolio.features.migrationV3.components.office;

import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import net.celloscope.mraims.loanportfolio.features.processmanagement.adapter.out.persistence.entity.OfficeEventTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteOfficeEventTrackerRepositoryV3 extends ReactiveCrudRepository<OfficeEventTrackerEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<OfficeEventTrackerEntity, String> {
    Mono<Void> deleteAllByManagementProcessIdAndOfficeEventNot(String managementProcessId, String officeEvent);
}
