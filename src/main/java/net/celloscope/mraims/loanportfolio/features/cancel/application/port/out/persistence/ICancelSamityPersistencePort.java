package net.celloscope.mraims.loanportfolio.features.cancel.application.port.out.persistence;

import net.celloscope.mraims.loanportfolio.features.cancel.domain.CancelSamity;
import reactor.core.publisher.Mono;

public interface ICancelSamityPersistencePort {

    Mono<CancelSamity> saveSamityEventTracker(CancelSamity cancelSamity);

}
