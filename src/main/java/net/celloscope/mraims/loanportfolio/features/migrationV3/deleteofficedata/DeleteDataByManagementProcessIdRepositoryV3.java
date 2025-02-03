package net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteDataByManagementProcessIdRepositoryV3<T, ID> extends ReactiveCrudRepository<T, ID> {
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
//    Mono<Void> deleteAllByManagementProcessIdAndOfficeEventNot(String managementProcessId, String officeEvent);
//    Mono<Void> deleteAllByOfficeId(String officeId);
}