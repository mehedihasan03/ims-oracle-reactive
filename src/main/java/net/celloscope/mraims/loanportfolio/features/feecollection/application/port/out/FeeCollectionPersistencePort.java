package net.celloscope.mraims.loanportfolio.features.feecollection.application.port.out;

import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeCollection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface FeeCollectionPersistencePort {
    Flux<FeeCollection> getFeeCollectionByOfficeIdAndManagementProcessId(String officeId, String managementProcessId);

    Flux<FeeCollection> saveAll(List<FeeCollection> feeCollections);

    Mono<List<FeeCollection>> rollbackFeeCollectionOnMISDayEndRevert(String officeId, String managementProcessId);
    Flux<FeeCollection> getFeeCollectionByOfficeIdAndManagementProcessIdOrManagementProcessIdIsNull(String officeId, String managementProcessId);

    Mono<Boolean> updateFeeCollectionStatusByManagementProcessId(String officeId, String managementProcessId, String status);
}
