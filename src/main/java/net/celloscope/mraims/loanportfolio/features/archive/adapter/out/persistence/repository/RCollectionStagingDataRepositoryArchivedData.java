package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RCollectionStagingDataRepositoryArchivedData extends RestoreArchivedDataBusiness<CollectionStagingDataEntity, String> {
}
