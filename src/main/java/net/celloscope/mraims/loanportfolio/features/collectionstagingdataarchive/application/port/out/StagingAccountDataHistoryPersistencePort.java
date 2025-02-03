package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.StagingAccountDataHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StagingAccountDataHistoryPersistencePort {

    Mono<Boolean> saveStagingAccountDataHistory(List<StagingAccountDataHistory> historyData);

    Flux<StagingAccountDataHistory> getLiveStagingAccountDataId(String officeId);

    Mono<StagingAccountDataHistory> getStagingAccountDataHistoryByLoanAccountIdAndManagementProcessId(String loanAccountId, String managementProcessId);
}
