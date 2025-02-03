package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.StagingData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StagingDataPersistencePort {

    Flux<StagingData> getStagingDataByOfficeId(String officeId);

    Mono<Boolean> deleteStagingDataById(String id);
}
