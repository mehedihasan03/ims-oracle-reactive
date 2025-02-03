package net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.out;

import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosure;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.domain.SavingsClosure;
import reactor.core.publisher.Mono;

public interface SavingsClosurePort {

    Mono<SavingsClosure> getSavingsClosureBySavingsAccountId(String savingsAccountId);

    Mono<SavingsClosure> saveSavingsClosure(SavingsClosure savingsClosure);

    Mono<Boolean> checkIfSavingsClosureExistsBySavingsAccountId(String savingsAccountId);

    Mono<SavingsClosure> updateSavingsClosureStatus(String savingsAccountId, String status, String loginId);
}
