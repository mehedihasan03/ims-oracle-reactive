package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.CollectionStagingData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CollectionStagingDataPersistencePort {

    Flux<CollectionStagingData> getCollectionStagingDataByOfficeId(String officeId);

    Mono<Boolean> deleteCollectionStagingDataById(String id);
}
