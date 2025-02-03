package net.celloscope.mraims.loanportfolio.features.feecollection.application.port.out;

import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeTypeSetting;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Mono;

public interface FeeTypeSettingPersistencePort {

    Mono<FeeTypeSetting> findByFeeSettingId(String feeSettingId, String officeId);
    Mono<FeeTypeSetting> findByFeeSettingId(String feeSettingId);
}
