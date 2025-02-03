package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity.WithdrawStagingDataHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IWithdrawHistoryRepositoryDelete extends ReactiveCrudRepository<WithdrawStagingDataHistoryEntity, String> , DeleteArchiveDataBusiness<WithdrawStagingDataHistoryEntity,String> {
}
