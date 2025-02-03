package net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out.repository.FeeTypeSettingRepository;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.out.FeeCollectionPersistencePort;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.out.FeeTypeSettingPersistencePort;
import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeTypeSetting;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FeeTypeSettingPersistenceAdapter implements FeeTypeSettingPersistencePort {
    private final FeeTypeSettingRepository repository;
    private final Gson gson;

    public FeeTypeSettingPersistenceAdapter(FeeTypeSettingRepository repository, Gson gson) {
        this.repository = repository;
        this.gson = CommonFunctions.buildGson(this);
    }


    @Override
    public Mono<FeeTypeSetting> findByFeeSettingId(String feeSettingId, String officeId) {
        return repository.findByFeeTypeSettingId(feeSettingId, officeId)
                .map(entity -> gson.fromJson(entity.toString(), FeeTypeSetting.class))
                .doOnNext(feeTypeSetting -> log.info("FeeTypeSetting findByFeeSettingId: {}", feeTypeSetting))
                .doOnError(throwable -> log.error("Error while FeeTypeSetting findByFeeSettingId: {}", throwable.getMessage()));
    }


    @Override
    public Mono<FeeTypeSetting> findByFeeSettingId(String feeSettingId) {
        return repository.findByFeeTypeSettingId(feeSettingId)
                .map(entity -> gson.fromJson(entity.toString(), FeeTypeSetting.class))
                .doOnNext(feeTypeSetting -> log.info("FeeTypeSetting findByFeeSettingId: {}", feeTypeSetting))
                .doOnError(throwable -> log.error("Error while FeeTypeSetting findByFeeSettingId: {}", throwable.getMessage()));
    }
}
