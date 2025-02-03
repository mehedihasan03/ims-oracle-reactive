package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherDetailHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IAutoVoucherDetailHistoryRepositoryDelete extends ReactiveCrudRepository<AutoVoucherDetailHistoryEntity, String> {
    Flux<AutoVoucherDetailHistoryEntity> findAllByVoucherId(String voucherId);
    Mono<Void> deleteAllByVoucherId(String voucherId);
}
