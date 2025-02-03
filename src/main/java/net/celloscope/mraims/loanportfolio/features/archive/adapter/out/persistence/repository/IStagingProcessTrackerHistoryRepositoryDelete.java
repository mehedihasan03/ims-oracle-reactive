package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity.StagingProcessTrackerHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IStagingProcessTrackerHistoryRepositoryDelete extends ReactiveCrudRepository<StagingProcessTrackerHistoryEntity, String> , DeleteArchiveDataBusiness<StagingProcessTrackerHistoryEntity,String> {
}
