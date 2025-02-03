package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.out.persistence.entity.StagingWithdrawDataEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RStagingWithdrawDataRepositoryArchivedData extends RestoreArchivedDataBusiness<StagingWithdrawDataEntity, String> {
}
