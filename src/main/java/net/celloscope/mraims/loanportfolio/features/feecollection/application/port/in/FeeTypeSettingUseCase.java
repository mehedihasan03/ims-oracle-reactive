package net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in;

import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeTypeSetting;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FeeTypeSettingUseCase {
    Mono<FeeTypeSetting> getFeeTypeSettingBySettingIdAndOfficeId(String feeSettingId, String officeId);
    Mono<FeeTypeSetting> getFeeTypeSettingBySettingId(String feeSettingId);
}
