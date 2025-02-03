package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.CollectionStagingDataHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CollectionStagingDataHistoryPersistencePort {

    Mono<Boolean> saveCollectionStagingDataHistory(List<CollectionStagingDataHistory> historyData);

    Flux<CollectionStagingDataHistory> getLiveCollectionStagingDataId(String officeId);


    Mono<CollectionStagingDataHistory> getCollectionStagingDataByLoanAccountIdAndManagementProcessId(String loanAccountId, String managementProcessId, String processId);
}
