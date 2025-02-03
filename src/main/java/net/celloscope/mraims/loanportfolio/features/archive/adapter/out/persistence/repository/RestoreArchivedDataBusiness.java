package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RestoreArchivedDataBusiness<T, ID> extends ReactiveCrudRepository<T, ID> {
}
