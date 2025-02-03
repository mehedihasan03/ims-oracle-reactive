package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity.StagingAccountDataHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.repository.StagingAccountDataHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.StagingAccountDataHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.StagingAccountDataHistory;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StagingAccountDataHistoryAdapter implements StagingAccountDataHistoryPersistencePort {

    private final ModelMapper modelMapper;

    private final StagingAccountDataHistoryRepository repository;

    @Override
    public Mono<Boolean> saveStagingAccountDataHistory(List<StagingAccountDataHistory> historyData) {
        return Mono.just(historyData.stream()
                        .map(archiveData -> this.modelMapper.map(archiveData, StagingAccountDataHistoryEntity.class))
                        .toList())
                .map(this.repository::saveAll)
                .mapNotNull(entity -> Boolean.TRUE)
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Flux<StagingAccountDataHistory> getLiveStagingAccountDataId(String officeId) {
        return this.repository.getByOfficeId(officeId)
                .mapNotNull(entity -> this.modelMapper.map(entity, StagingAccountDataHistory.class))
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Mono<StagingAccountDataHistory> getStagingAccountDataHistoryByLoanAccountIdAndManagementProcessId(String loanAccountId, String managementProcessId) {
        return repository
                .findByLoanAccountIdAndManagementProcessId(loanAccountId, managementProcessId)
                .map(entity -> modelMapper.map(entity, StagingAccountDataHistory.class))
                .doOnError(throwable -> log.error("Exception encountered in getStagingAccountDataByLoanAccountIdAndManagementProcessId\nReason - {}", throwable.getMessage()))
                .onErrorMap(throwable -> new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while fetching staging account data"))
                .doOnNext(dto -> log.info("after map staging account dto : {}", dto));
    }
}
