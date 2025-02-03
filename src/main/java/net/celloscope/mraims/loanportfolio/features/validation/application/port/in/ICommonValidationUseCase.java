package net.celloscope.mraims.loanportfolio.features.validation.application.port.in;

import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICommonValidationUseCase {

    Mono<ManagementProcessTracker> validateStagingDataGenerationRequestForOffice(String officeId);
    Mono<Boolean> validateSamityStagingDataInvalidationRequestForSamityList(String officeId, List<String> samityIdList);
}
