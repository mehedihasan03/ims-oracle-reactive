package net.celloscope.mraims.loanportfolio.features.feecollection.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.FeeTypeSettingUseCase;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.out.FeeTypeSettingPersistencePort;
import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeTypeSetting;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class FeeTypeSettingService implements FeeTypeSettingUseCase {

    private final FeeTypeSettingPersistencePort feeTypeSettingPersistencePort;

    public FeeTypeSettingService(FeeTypeSettingPersistencePort feeTypeSettingPersistencePort) {
        this.feeTypeSettingPersistencePort = feeTypeSettingPersistencePort;
    }

    @Override
    public Mono<FeeTypeSetting> getFeeTypeSettingBySettingIdAndOfficeId(String feeSettingId, String officeId) {
        return feeTypeSettingPersistencePort.findByFeeSettingId(feeSettingId, officeId);
    }


    @Override
    public Mono<FeeTypeSetting> getFeeTypeSettingBySettingId(String feeSettingId) {
        return feeTypeSettingPersistencePort.findByFeeSettingId(feeSettingId);
    }
}
