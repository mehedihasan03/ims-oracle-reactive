package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DeleteArchiveDataBusiness<T, ID> extends ReactiveCrudRepository<T, ID> {
    Flux<T> findAllByManagementProcessId(String managementProcessId);
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
