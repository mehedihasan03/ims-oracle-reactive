package net.celloscope.mraims.loanportfolio.features.fdr.application.port.out;

import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDRClosure;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FDRClosurePersistencePort {
    Mono<FDRClosure> saveFDRClosure(FDRClosure fdrClosure);
    Mono<FDRClosure> getFDRClosureBySavingsAccountId(String savingsAccountId);
    Mono<FDRClosure> updateFDRClosureStatus(String savingsAccountId, String status, String loginId);
    Mono<Boolean> checkIfFDRClosureExistsBySavingsAccountId(String savingsAccountId);

    Flux<FDRClosure> getAllFDRClosureByOfficeId(String officeId);
}
