package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service;

import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.WithdrawStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.helpers.dto.WithdrawStagingData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Mono;


@Service
public class DataArchiveValidationService {

    private final CollectionStagingDataPersistencePort collectionStagingDataPersistencePort;

    private final WithdrawStagingDataPersistencePort withdrawStagingDataPersistencePort;

    public DataArchiveValidationService(
            @Qualifier("CollectionStagingDataArchiveAdapter") CollectionStagingDataPersistencePort collectionStagingDataPersistencePort,
            WithdrawStagingDataPersistencePort withdrawStagingDataPersistencePort) {
        this.collectionStagingDataPersistencePort = collectionStagingDataPersistencePort;
        this.withdrawStagingDataPersistencePort = withdrawStagingDataPersistencePort;
    }

    public Mono<Boolean> isValidWithdrawStagingData(String officeId) {
        return this.withdrawStagingDataPersistencePort.getWithdrawStagingDataByOfficeId(officeId)
                .mapNotNull(WithdrawStagingData::getStatus)
                .collectList()
                .mapNotNull(x -> x.stream().allMatch(d -> d.equals(Status.STATUS_APPROVED.getValue())));
    }

    public Mono<Boolean> isValidCollectionStagingData(String officeId) {
        return this.collectionStagingDataPersistencePort.getCollectionStagingDataByOfficeId(officeId)
                .mapNotNull(CollectionStagingData::getStatus)
                .collectList()
                .mapNotNull(x -> x.stream().allMatch(d -> d.equals(Status.STATUS_APPROVED.getValue())));
    }
}
