package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEditHistoryEntity;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.repository.CollectionStagingDataEditHistoryRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataEditHistoryPort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingDataEditHistory;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CollectionStagingDataEditHistoryAdapter implements CollectionStagingDataEditHistoryPort {

    private final CollectionStagingDataEditHistoryRepository editHistoryRepository;
    private final ModelMapper mapper;

    public CollectionStagingDataEditHistoryAdapter(CollectionStagingDataEditHistoryRepository editHistoryRepository, ModelMapper mapper) {
        this.editHistoryRepository = editHistoryRepository;
        this.mapper = mapper;
    }

    @Override
    public Mono<CollectionStagingDataEditHistory> saveCollectionStagingDataEditHistory(CollectionStagingDataEditHistory collectionStagingDataEditHistory) {
        return Mono.just(collectionStagingDataEditHistory)
                .map(data -> mapper.map(data, CollectionStagingDataEditHistoryEntity.class))
                .flatMap(editHistoryRepository::save)
                .map(entity -> mapper.map(entity, CollectionStagingDataEditHistory.class));
    }
}
