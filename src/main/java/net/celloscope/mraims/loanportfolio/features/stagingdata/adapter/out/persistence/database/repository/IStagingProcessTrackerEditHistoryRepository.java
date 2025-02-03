package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.repository;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEditHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface IStagingProcessTrackerEditHistoryRepository extends ReactiveCrudRepository<StagingProcessTrackerEditHistoryEntity, String> {
}
