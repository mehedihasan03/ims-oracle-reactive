package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity.CollectionStagingDataHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.repository.CollectionStagingDataHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.CollectionStagingDataHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.CollectionStagingDataHistory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CollectionStagingDataHistoryAdapter implements CollectionStagingDataHistoryPersistencePort {

    private final ModelMapper modelMapper;

    private final CollectionStagingDataHistoryRepository repository;

    @Override
    public Mono<Boolean> saveCollectionStagingDataHistory(List<CollectionStagingDataHistory> historyData) {
        return Mono.just(historyData.stream()
                        .map(archiveData -> this.modelMapper.map(archiveData, CollectionStagingDataHistoryEntity.class))
                        .toList())
                .map(this.repository::saveAll)
                .mapNotNull(entity -> Boolean.TRUE)
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Flux<CollectionStagingDataHistory> getLiveCollectionStagingDataId(String officeId) {
        return this.repository.getByOfficeId(officeId, Status.STATUS_APPROVED.getValue())
                .mapNotNull(entity -> this.modelMapper.map(entity, CollectionStagingDataHistory.class))
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Mono<CollectionStagingDataHistory> getCollectionStagingDataByLoanAccountIdAndManagementProcessId(String loanAccountId,
                                                                                                            String managementProcessId,
                                                                                                            String processId) {
        return repository.findFirstByLoanAccountIdAndManagementProcessIdAndProcessIdOrderByCreatedOnDesc(loanAccountId, managementProcessId, processId)
                .doOnNext(entity -> log.info("Collection Staging Data History: {}", entity))
                .map(entity -> modelMapper.map(entity, CollectionStagingDataHistory.class));
    }
}
