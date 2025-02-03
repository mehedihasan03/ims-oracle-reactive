package net.celloscope.mraims.loanportfolio.features.passbookhistory.application.port.out;

import net.celloscope.mraims.loanportfolio.features.passbookhistory.domain.PassbookHistory;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PassbookHistoryPersistencePort {
    Mono<Boolean> archivePassbookHistory(List<PassbookHistory> passbookHistories);
}
