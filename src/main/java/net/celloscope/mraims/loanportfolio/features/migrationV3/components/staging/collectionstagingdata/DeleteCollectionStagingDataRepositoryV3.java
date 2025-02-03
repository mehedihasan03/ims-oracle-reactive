package net.celloscope.mraims.loanportfolio.features.migrationV3.components.staging.collectionstagingdata;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteDataByManagementProcessIdRepositoryV3;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DeleteCollectionStagingDataRepositoryV3 extends ReactiveCrudRepository<CollectionStagingDataEntity, String>, DeleteDataByManagementProcessIdRepositoryV3<CollectionStagingDataEntity, String> {
    @Override
    Mono<Void> deleteAllByManagementProcessId(String managementProcessId);
}
