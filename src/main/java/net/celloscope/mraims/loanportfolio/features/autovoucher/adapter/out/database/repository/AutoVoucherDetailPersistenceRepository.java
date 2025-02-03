package net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherDetailEntity;
import net.celloscope.mraims.loanportfolio.features.autovoucher.domain.AutoVoucherDetail;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AutoVoucherDetailPersistenceRepository extends ReactiveCrudRepository<AutoVoucherDetailEntity, String> {

    Flux<AutoVoucherDetailEntity> findAllByVoucherId(String voucherId);
    Mono<Boolean> deleteAllByVoucherIdIn(List<String> voucherIdList);
}
