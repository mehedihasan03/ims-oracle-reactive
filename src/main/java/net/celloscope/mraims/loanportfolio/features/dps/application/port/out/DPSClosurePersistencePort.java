package net.celloscope.mraims.loanportfolio.features.dps.application.port.out;

import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosure;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DPSClosurePersistencePort {
    Mono<Boolean> checkIfDPSClosureExistsBySavingsAccountId(String savingsAccountId);
    Mono<DPSClosure> saveDPSClosure(DPSClosure dpsClosure);
    Mono<DPSClosure> getDPSClosureBySavingsAccountId(String savingsAccountId);
    Mono<DPSClosure> updateDPSClosureStatus(String savingsAccountId, String status, String loginId);
    Flux<DPSClosure> getAllDPSClosureByOfficeId(String officeId);
}
