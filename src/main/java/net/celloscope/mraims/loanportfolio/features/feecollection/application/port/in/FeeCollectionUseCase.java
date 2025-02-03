package net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in;

import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.dto.request.FeeCollectionUpdateRequestDTO;
import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeCollection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface FeeCollectionUseCase {

    Mono<List<FeeCollection>> updateNullableFeeCollectionByOfficeId(FeeCollectionUpdateRequestDTO requestDTO);
    Flux<FeeCollection> getFeeCollectionByOfficeId(String officeId);
    Mono<List<FeeCollection>> rollbackFeeCollectionOnMISDayEndRevert(String officeId, String managementProcessId);
    Flux<FeeCollection> getFeeCollectionByOfficeIdForCurrentDay(String officeId);
    Mono<Boolean> updateFeeCollectionStatusByManagementProcessId(String officeId, String managementProcessId, String status);
}
