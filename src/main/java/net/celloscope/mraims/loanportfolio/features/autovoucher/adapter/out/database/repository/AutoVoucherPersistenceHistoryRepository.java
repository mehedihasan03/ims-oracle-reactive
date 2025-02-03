package net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.repository;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface AutoVoucherPersistenceHistoryRepository  extends ReactiveCrudRepository<AutoVoucherHistoryEntity, String> {
}
