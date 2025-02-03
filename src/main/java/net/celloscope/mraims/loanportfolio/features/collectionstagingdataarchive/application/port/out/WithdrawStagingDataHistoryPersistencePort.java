package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.WithdrawStagingDataHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface WithdrawStagingDataHistoryPersistencePort {

    Mono<Boolean> saveWithdrawStagingDataHistory(List<WithdrawStagingDataHistory> historyData);

    Flux<WithdrawStagingDataHistory> getLiveWithdrawStagingDataId(String officeId);
}
