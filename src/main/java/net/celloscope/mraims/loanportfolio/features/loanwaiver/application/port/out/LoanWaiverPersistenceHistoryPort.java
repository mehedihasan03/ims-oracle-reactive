package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.out;

import net.celloscope.mraims.loanportfolio.features.loanwaiver.domain.LoanWaiver;
import reactor.core.publisher.Mono;

public interface LoanWaiverPersistenceHistoryPort {
    Mono<LoanWaiver> saveLoanWaiverHistory(LoanWaiver loanWaiver);

    Mono<LoanWaiver> getLoanWaiverHistoryById(String loanWaiverOid);

}
