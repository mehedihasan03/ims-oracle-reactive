package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.StagingDataHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.StagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.StagingDataHistory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class StagingDataArchiveService {

    private final StagingDataPersistencePort dataPersistencePort;

    private final StagingDataHistoryPersistencePort dataHistoryPersistencePort;

    private final ModelMapper modelMapper;

    public StagingDataArchiveService(
            @Qualifier("StagingDataArchiveAdapter") StagingDataPersistencePort dataPersistencePort,
            StagingDataHistoryPersistencePort dataHistoryPersistencePort,
            ModelMapper modelMapper) {
        this.dataPersistencePort = dataPersistencePort;
        this.dataHistoryPersistencePort = dataHistoryPersistencePort;
        this.modelMapper = modelMapper;
    }

    public Mono<Boolean> archiveStagingDataIntoHistory(String officeId) {
        return dataPersistencePort.getStagingDataByOfficeId(officeId)
                .mapNotNull(liveData -> modelMapper.map(liveData, StagingDataHistory.class))
                .collectList()
                .flatMap(dataHistoryPersistencePort::saveStagingDataHistory)
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(x -> log.info("{}", x));
    }

    public Mono<Boolean> deleteLiveStagingData(String officeId) {
        return dataHistoryPersistencePort.getLiveStagingDataId(officeId)
                .mapNotNull(StagingDataHistory::getStagingDataId)
                .flatMap(dataPersistencePort::deleteStagingDataById)
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(x -> log.info("{}", x))
                .collectList()
                .mapNotNull(deleteStatus -> deleteStatus.stream().noneMatch(s -> s.equals(Boolean.FALSE)));
    }
}
