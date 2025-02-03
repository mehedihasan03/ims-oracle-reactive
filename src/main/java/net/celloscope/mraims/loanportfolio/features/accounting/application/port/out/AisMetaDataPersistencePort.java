package net.celloscope.mraims.loanportfolio.features.accounting.application.port.out;

import net.celloscope.mraims.loanportfolio.features.accounting.domain.AisMetaData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AisMetaDataPersistencePort {
    Flux<AisMetaData> getAisMetaDataByProcessName(String processName);
}
