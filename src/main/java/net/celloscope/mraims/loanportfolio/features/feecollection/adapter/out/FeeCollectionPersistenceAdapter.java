package net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out.entity.FeeCollectionEntity;
import net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out.repository.FeeCollectionRepository;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.out.FeeCollectionPersistencePort;
import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeCollection;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class FeeCollectionPersistenceAdapter implements FeeCollectionPersistencePort {
    private final FeeCollectionRepository repository;
    private final Gson gson;

    public FeeCollectionPersistenceAdapter(FeeCollectionRepository repository, Gson gson) {
        this.repository = repository;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Flux<FeeCollection> getFeeCollectionByOfficeIdAndManagementProcessId(String officeId, String managementProcessId) {
        return repository.findAllByOfficeIdAndManagementProcessId(officeId, managementProcessId)
                .map(entity -> gson.fromJson(entity.toString(), FeeCollection.class))
                .doOnNext(feeCollection -> log.info("FeeCollection getFeeCollectionByOfficeId: {}", feeCollection))
                .doOnError(throwable -> log.error("Error while FeeCollection getFeeCollectionByOfficeId: {}", throwable.getMessage()));
    }

    @Override
    public Flux<FeeCollection> getFeeCollectionByOfficeIdAndManagementProcessIdOrManagementProcessIdIsNull(String officeId, String managementProcessId) {
        log.info("FeeCollection : officeId: {}, managementProcessId: {}", officeId, managementProcessId);
        return repository.findAllByOfficeIdAndManagementProcessIdOrManagementProcessIdIsNull(officeId, managementProcessId)
                .map(entity -> gson.fromJson(entity.toString(), FeeCollection.class))
                .doOnNext(feeCollection -> log.info("FeeCollection getFeeCollectionByOfficeId: {}", feeCollection))
                .doOnError(throwable -> log.error("Error while FeeCollection getFeeCollectionByOfficeId: {}", throwable.getMessage()));
    }

    @Override
    public Mono<Boolean> updateFeeCollectionStatusByManagementProcessId(String officeId, String managementProcessId, String status) {
        return repository.findAllByOfficeIdAndManagementProcessId(officeId, managementProcessId)
                .map(entity -> {
                    entity.setStatus(status);
                    return entity;
                })
                .flatMap(repository::save)
                .collectList()
                .map(feeCollectionEntity -> true)
                .doOnNext(isUpdated -> log.info("FeeCollection updateFeeCollectionStatusByManagementProcessId: {}", isUpdated))
                .doOnError(throwable -> log.error("Error while FeeCollection updateFeeCollectionStatusByManagementProcessId: {}", throwable.getMessage()));
    }

    @Override
    public Flux<FeeCollection> saveAll(List<FeeCollection> feeCollections) {
        return Flux.fromIterable(feeCollections)
                .map(feeCollection -> gson.fromJson(gson.toJson(feeCollection), FeeCollectionEntity.class))
                .collectList()
                .flatMapMany(repository::saveAll)
                .map(entity -> gson.fromJson(entity.toString(), FeeCollection.class))
                .doOnNext(feeCollection -> log.info("FeeCollection saveAll: {}", feeCollection))
                .doOnError(throwable -> log.error("Error while FeeCollection saveAll: {}", throwable.getMessage()));
    }

    @Override
    public Mono<List<FeeCollection>> rollbackFeeCollectionOnMISDayEndRevert(String officeId, String managementProcessId) {
        return repository.rollbackFeeCollectionOnMISDayEndRevert(officeId, managementProcessId, Status.STATUS_PENDING.getValue())
                .map(entity -> gson.fromJson(entity.toString(), FeeCollection.class))
                .collectList()
                .doOnNext(feeCollections -> log.info("FeeCollection rollbackFeeCollectionOnMISDayEndRevert: {}", feeCollections))
                .doOnError(throwable -> log.error("Error while FeeCollection rollbackFeeCollectionOnMISDayEndRevert: {}", throwable.getMessage()));
    }
}
