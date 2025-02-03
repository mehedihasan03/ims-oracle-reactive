package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.repository.StagingAccountDataRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.StagingAccountDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto.StagingAccountData;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class StagingAccountDataGatewayAdapter implements StagingAccountDataPersistencePort {
    private final ModelMapper mapper;
    private final StagingAccountDataRepository repository;

    @Override
    public Flux<StagingAccountData> getAllStagingAccountDataByListOfMemberId(List<String> memberId) {
        return repository
                .findAllByMemberIdIn(memberId)
                .map(stagingAccountDataEntity -> mapper.map(stagingAccountDataEntity, StagingAccountData.class))
                .doOnComplete(() -> log.info("List of StagingAccountDataEntity fetched from Db "))
                .doOnError(e -> log.error("Error while fetching StagingAccountDataEntity from Db\nReason - {}", e.getMessage()))
                ;
    }

    @Override
    public Mono<StagingAccountData> getStagingAccountDataByLoanAccountIdAndManagementProcessId(String loanAccountId, String managementProcessId) {
        return repository
                .findByLoanAccountIdAndManagementProcessId(loanAccountId, managementProcessId)
                .map(entity -> mapper.map(entity, StagingAccountData.class))
                .doOnError(throwable -> log.error("Exception encountered in getStagingAccountDataByLoanAccountIdAndManagementProcessId\nReason - {}", throwable.getMessage()))
                .onErrorMap(throwable -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while fetching staging account data"))
                .doOnNext(dto -> log.debug("after map staging account dto : {}", dto));
    }
}
