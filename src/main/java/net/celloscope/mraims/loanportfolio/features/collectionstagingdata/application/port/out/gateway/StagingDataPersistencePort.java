package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.StagingData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StagingDataPersistencePort {
    Flux<StagingData> getListOfStagingDataByFieldOfficerId(String fieldOfficerId);

    Flux<StagingData> getListOfStagingDataBySamityId(String samityId);

    Flux<StagingData> getListOfStagingDataByFieldOfficerIdAndSamityDay(String fieldOfficerId, String samityDay);

    Flux<StagingData> getListOfStagingDataByMfiId(String mfiId);

    Flux<StagingData> getListOfStagingDataByFieldOfficerIdForNonSamityDay(String fieldOfficerId, String samityDay);

    Flux<StagingData> getListOfStagingDataBySamityIdIdAndSamityDay(String samityId, String samityDay);

    Mono<StagingData> getOneStagingDataEntityBySamityId(String samityId);
}
