package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.repository.CollectionStagingDataArchiveRepository;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.CollectionStagingData;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Qualifier("CollectionStagingDataArchiveAdapter")
public class CollectionStagingDataArchiveAdapter implements CollectionStagingDataPersistencePort {

    private final ModelMapper modelMapper;

    private final CollectionStagingDataArchiveRepository repository;

    public CollectionStagingDataArchiveAdapter(ModelMapper modelMapper, CollectionStagingDataArchiveRepository repository) {
        this.modelMapper = modelMapper;
        this.repository = repository;
    }

    @Override
    public Flux<CollectionStagingData> getCollectionStagingDataByOfficeId(String officeId) {
        return this.repository.getByOfficeId(officeId)
                .mapNotNull(entity -> this.modelMapper.map(entity, CollectionStagingData.class))
                .doOnError(error -> log.error(error.getMessage()));
    }

    @Override
    public Mono<Boolean> deleteCollectionStagingDataById(String id) {
        return this.repository.deleteByCollectionStagingDataId(id)
                .mapNotNull(voidResult -> Boolean.TRUE)
                .doOnError(error -> log.error(error.getMessage()));
    }
}
