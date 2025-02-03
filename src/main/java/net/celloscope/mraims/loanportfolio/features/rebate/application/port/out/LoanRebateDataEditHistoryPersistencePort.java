package net.celloscope.mraims.loanportfolio.features.rebate.application.port.out;

import net.celloscope.mraims.loanportfolio.features.rebate.domain.LoanRebate;
import reactor.core.publisher.Mono;

public interface LoanRebateDataEditHistoryPersistencePort {
    Mono<LoanRebate> saveLoanRebateEditHistory(LoanRebate loanRebate);
    Mono<LoanRebate> getLastLoanRebateEditHistoryByLoanRebateDataId(String loanRebateDataOid);
}
