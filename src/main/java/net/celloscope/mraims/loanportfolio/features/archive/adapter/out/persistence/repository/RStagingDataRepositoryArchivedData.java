package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingDataEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RStagingDataRepositoryArchivedData extends RestoreArchivedDataBusiness<StagingDataEntity, String> {
}
