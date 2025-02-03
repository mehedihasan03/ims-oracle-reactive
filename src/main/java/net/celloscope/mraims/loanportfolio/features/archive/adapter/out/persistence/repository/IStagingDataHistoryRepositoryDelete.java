package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity.StagingDataHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IStagingDataHistoryRepositoryDelete extends ReactiveCrudRepository<StagingDataHistoryEntity, String> , DeleteArchiveDataBusiness<StagingDataHistoryEntity,String> {
}
