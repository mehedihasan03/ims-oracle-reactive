package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway;

import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.StagingAccountData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StagingAccountDataPersistencePort {
    Flux<StagingAccountData> getAllStagingAccountDataByListOfMemberId(List<String> memberId);

    Mono<StagingAccountData> getStagingAccountDataByLoanAccountIdAndManagementProcessId(String loanAccountId, String managementProcessId);


}
