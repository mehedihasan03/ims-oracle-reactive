package net.celloscope.mraims.loanportfolio.features.rebate.application.port.out;

import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import reactor.core.publisher.Mono;

public interface LoanRebateHistoryPersistencePort {
    Mono<LoanRebate> saveLoanRebateHistory(LoanRebate loanRebate);
    Mono<LoanRebate> getLastLoanRebateHistoryByLoanRebateDataOid(String loanRebateDataOid);
}
