package net.celloscope.mraims.loanportfolio.features.migration.deleteofficedata;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DeleteDataByManagementProcessIdRepository <T, ID> extends ReactiveCrudRepository<T, ID> {
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
//    Mono<Void> deleteAllByManagementProcessIdAndOfficeEventNot(String managementProcessId, String officeEvent);
//    Mono<Void> deleteAllByOfficeId(String officeId);
}