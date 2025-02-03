package net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherDetailHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AutoVoucherDetailPersistenceHistoryRepository extends ReactiveCrudRepository<AutoVoucherDetailHistoryEntity, String> {
}
