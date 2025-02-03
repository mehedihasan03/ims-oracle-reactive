package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.CollectionStagingDataHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.CollectionStagingDataHistory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CollectionStagingDataArchiveService {

    private final CollectionStagingDataPersistencePort dataPersistencePort;

    private final CollectionStagingDataHistoryPersistencePort dataHistoryPersistencePort;

    private final ModelMapper modelMapper;

    public CollectionStagingDataArchiveService(
            @Qualifier("CollectionStagingDataArchiveAdapter") CollectionStagingDataPersistencePort dataPersistencePort,
            CollectionStagingDataHistoryPersistencePort dataHistoryPersistencePort,
            ModelMapper modelMapper) {
        this.dataPersistencePort = dataPersistencePort;
        this.dataHistoryPersistencePort = dataHistoryPersistencePort;
        this.modelMapper = modelMapper;
    }

    public Mono<Boolean> archiveCollectionStagingDataIntoHistory(String officeId) {
        return dataPersistencePort.getCollectionStagingDataByOfficeId(officeId)
                .mapNotNull(liveData -> modelMapper.map(liveData, CollectionStagingDataHistory.class))
                .collectList()
                .flatMap(dataHistoryPersistencePort::saveCollectionStagingDataHistory)
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(x -> log.info("{}", x));
    }

    public Mono<Boolean> deleteLiveCollectionStagingData(String officeId) {
        return dataHistoryPersistencePort.getLiveCollectionStagingDataId(officeId)
                .mapNotNull(CollectionStagingDataHistory::getCollectionStagingDataId)
                .flatMap(dataPersistencePort::deleteCollectionStagingDataById)
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(x -> log.info("{}", x))
                .collectList()
                .mapNotNull(deleteStatus -> deleteStatus.stream().noneMatch(s -> s.equals(Boolean.FALSE)));
    }
}
