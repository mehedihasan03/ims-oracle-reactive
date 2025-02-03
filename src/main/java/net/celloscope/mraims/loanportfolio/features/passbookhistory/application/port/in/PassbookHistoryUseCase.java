package net.celloscope.mraims.loanportfolio.features.passbookhistory.application.port.in;

import net.celloscope.mraims.loanportfolio.features.passbook.domain.Passbook;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PassbookHistoryUseCase {
    Mono<Boolean> archivePassbookHistory(List<Passbook> passbookList, String loginId);
}
