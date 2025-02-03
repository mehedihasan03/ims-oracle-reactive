package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.WithdrawStagingData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WithdrawStagingDataPersistencePort {

    Flux<WithdrawStagingData> getWithdrawStagingDataByOfficeId(String officeId);

    Mono<Boolean> deleteWithdrawStagingDataById(String id);
}
