package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity.CollectionStagingDataHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ICollectionHistoryRepositoryDelete extends ReactiveCrudRepository<CollectionStagingDataHistoryEntity,String> , DeleteArchiveDataBusiness<CollectionStagingDataHistoryEntity,String> {
}
