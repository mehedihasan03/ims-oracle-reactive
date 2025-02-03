package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity.StagingDataHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.repository.StagingDataHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.StagingDataHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.StagingDataHistory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StagingDataHistoryAdapter implements StagingDataHistoryPersistencePort {

    private final ModelMapper modelMapper;

    private final StagingDataHistoryRepository repository;

    @Override
    public Mono<Boolean> saveStagingDataHistory(List<StagingDataHistory> historyData) {
        return Mono.just(historyData.stream()
                        .map(archiveData -> this.modelMapper.map(archiveData, StagingDataHistoryEntity.class))
                        .toList())
                .map(this.repository::saveAll)
                .mapNotNull(entity -> Boolean.TRUE)
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Flux<StagingDataHistory> getLiveStagingDataId(String officeId) {
        return this.repository.getByOfficeId(officeId)
                .mapNotNull(entity -> this.modelMapper.map(entity, StagingDataHistory.class))
                .doOnError(error -> log.error(error.getMessage()));
    }
}
