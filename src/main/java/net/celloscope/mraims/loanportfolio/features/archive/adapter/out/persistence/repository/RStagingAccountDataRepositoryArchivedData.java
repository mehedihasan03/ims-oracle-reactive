package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingAccountDataEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RStagingAccountDataRepositoryArchivedData extends RestoreArchivedDataBusiness<StagingAccountDataEntity, String> {
}
