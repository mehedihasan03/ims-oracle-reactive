package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.StagingDataHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StagingDataHistoryPersistencePort {

    Mono<Boolean> saveStagingDataHistory(List<StagingDataHistory> historyData);

    Flux<StagingDataHistory> getLiveStagingDataId(String officeId);
}
