package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.StagingDataHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.WithdrawStagingDataHistoryPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.StagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.WithdrawStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.StagingDataHistory;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain.WithdrawStagingDataHistory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class WithdrawStagingDataArchiveService {

    private final WithdrawStagingDataPersistencePort dataPersistencePort;

    private final WithdrawStagingDataHistoryPersistencePort dataHistoryPersistencePort;

    private final ModelMapper modelMapper;

    public WithdrawStagingDataArchiveService(
            @Qualifier("WithdrawStagingDataArchiveAdapter") WithdrawStagingDataPersistencePort dataPersistencePort,
            WithdrawStagingDataHistoryPersistencePort dataHistoryPersistencePort,
            ModelMapper modelMapper) {
        this.dataPersistencePort = dataPersistencePort;
        this.dataHistoryPersistencePort = dataHistoryPersistencePort;
        this.modelMapper = modelMapper;
    }

    public Mono<Boolean> archiveWithdrawStagingDataIntoHistory(String officeId) {
        return dataPersistencePort.getWithdrawStagingDataByOfficeId(officeId)
                .mapNotNull(liveData -> modelMapper.map(liveData, WithdrawStagingDataHistory.class))
                .collectList()
                .flatMap(dataHistoryPersistencePort::saveWithdrawStagingDataHistory)
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(x -> log.info("{}", x));
    }

    public Mono<Boolean> deleteLiveWithdrawStagingData(String officeId) {
        return dataHistoryPersistencePort.getLiveWithdrawStagingDataId(officeId)
                .mapNotNull(WithdrawStagingDataHistory::getWithdrawStagingDataId)
                .flatMap(dataPersistencePort::deleteWithdrawStagingDataById)
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(x -> log.info("{}", x))
                .collectList()
                .mapNotNull(deleteStatus -> deleteStatus.stream().noneMatch(s -> s.equals(Boolean.FALSE)));
    }
}
