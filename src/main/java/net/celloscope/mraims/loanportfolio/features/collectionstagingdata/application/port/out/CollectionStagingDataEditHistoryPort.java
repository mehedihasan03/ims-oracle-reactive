package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingDataEditHistory;
import reactor.core.publisher.Mono;

public interface CollectionStagingDataEditHistoryPort {
    Mono<CollectionStagingDataEditHistory> saveCollectionStagingDataEditHistory(CollectionStagingDataEditHistory dataEditHistory);
}
