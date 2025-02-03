package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity.AutoVoucherHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IAutoVoucherHistoryRepositoryDelete extends ReactiveCrudRepository<AutoVoucherHistoryEntity, String>, DeleteArchiveDataBusiness<AutoVoucherHistoryEntity,String> {
}
