package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.StagingAccountData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StagingAccountDataPersistencePort {

    Flux<StagingAccountData> getStagingAccountDataByOfficeId(String officeId);

    Mono<Boolean> deleteStagingAccountDataById(String id);
}
