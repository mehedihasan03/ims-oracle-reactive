package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.StagingAccountDataHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.StagingAccountDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.StagingAccountDataHistory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class StagingAccountDataArchiveService {

    private final StagingAccountDataPersistencePort dataPersistencePort;

    private final StagingAccountDataHistoryPersistencePort dataHistoryPersistencePort;

    private final ModelMapper modelMapper;

    public StagingAccountDataArchiveService(
            @Qualifier("StagingAccountDataArchiveAdapter") StagingAccountDataPersistencePort dataPersistencePort,
            StagingAccountDataHistoryPersistencePort dataHistoryPersistencePort,
            ModelMapper modelMapper) {
        this.dataPersistencePort = dataPersistencePort;
        this.dataHistoryPersistencePort = dataHistoryPersistencePort;
        this.modelMapper = modelMapper;
    }


    public Mono<Boolean> archiveStagingAccountDataIntoHistory(String officeId) {
        return dataPersistencePort.getStagingAccountDataByOfficeId(officeId)
                .mapNotNull(liveData -> modelMapper.map(liveData, StagingAccountDataHistory.class))
                .collectList()
                .flatMap(dataHistoryPersistencePort::saveStagingAccountDataHistory)
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(x -> log.info("{}", x));
    }

    public Mono<Boolean> deleteLiveStagingAccountData(String officeId) {
        return dataHistoryPersistencePort.getLiveStagingAccountDataId(officeId)
                .mapNotNull(StagingAccountDataHistory::getStagingAccountDataId)
                .flatMap(dataPersistencePort::deleteStagingAccountDataById)
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(x -> log.info("{}", x))
                .collectList()
                .mapNotNull(deleteStatus -> deleteStatus.stream().noneMatch(s -> s.equals(Boolean.FALSE)));
    }
}
