package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.out;

import net.celloscope.mraims.loanportfolio.features.loanadjustment.domain.LoanAdjustmentDataEditHistory;
import reactor.core.publisher.Mono;

public interface LoanAdjustmentEditHistoryPort {

    Mono<LoanAdjustmentDataEditHistory> saveAdjustmentEditHistory(LoanAdjustmentDataEditHistory loanAdjustmentDataEditHistory);
}
