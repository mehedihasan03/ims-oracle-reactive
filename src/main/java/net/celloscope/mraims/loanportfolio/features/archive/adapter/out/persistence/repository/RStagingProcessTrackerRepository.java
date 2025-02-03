package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RStagingProcessTrackerRepository extends RestoreArchivedDataBusiness<StagingProcessTrackerEntity, String> {
}
