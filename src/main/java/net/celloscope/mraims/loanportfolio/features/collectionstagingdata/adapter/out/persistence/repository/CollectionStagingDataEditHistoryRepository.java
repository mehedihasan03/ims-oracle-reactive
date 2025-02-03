package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEditHistoryEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CollectionStagingDataEditHistoryRepository extends ReactiveCrudRepository<CollectionStagingDataEditHistoryEntity, String> {
}
