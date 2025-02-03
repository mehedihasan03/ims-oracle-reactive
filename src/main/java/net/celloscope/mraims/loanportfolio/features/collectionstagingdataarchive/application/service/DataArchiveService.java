package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.service;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.DataArchiveUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.helpers.dto.commands.DataArchiveCommandDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.helpers.dto.response.DataArchiveResponseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class DataArchiveService implements DataArchiveUseCase {

    private final DataArchiveValidationService dataArchiveValidationService;

    private final StagingDataArchiveService stagingDataArchiveService;

    private final WithdrawStagingDataArchiveService withdrawStagingDataArchiveService;

    private final StagingAccountDataArchiveService stagingAccountDataArchiveService;

    private final CollectionStagingDataArchiveService collectionStagingDataArchiveService;

    private final TransactionalOperator rxtx;
    private final ReactiveValueOperations<String, String> reactiveValueOps;

    public DataArchiveService(
            TransactionalOperator rxtx,
            ReactiveValueOperations<String, String> reactiveValueOps,
            DataArchiveValidationService dataArchiveValidationService,
            StagingDataArchiveService stagingDataArchiveService,
            WithdrawStagingDataArchiveService withdrawStagingDataArchiveService,
            StagingAccountDataArchiveService stagingAccountDataArchiveService,
            CollectionStagingDataArchiveService collectionStagingDataArchiveService) {
        this.rxtx = rxtx;
        this.reactiveValueOps = reactiveValueOps;
        this.dataArchiveValidationService = dataArchiveValidationService;
        this.stagingDataArchiveService = stagingDataArchiveService;
        this.withdrawStagingDataArchiveService = withdrawStagingDataArchiveService;
        this.stagingAccountDataArchiveService = stagingAccountDataArchiveService;
        this.collectionStagingDataArchiveService = collectionStagingDataArchiveService;
    }

    @Override
    public Mono<DataArchiveResponseDto> archive(DataArchiveCommandDto command) {
        return dataArchiveValidationService.isValidCollectionStagingData(command.getOfficeId())
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#01(a). archive: isValidCollectionStagingData? {}", c.toString()))
                .flatMap(c -> {
                    if (c.equals(Boolean.TRUE)) {
                        return dataArchiveValidationService.isValidWithdrawStagingData(command.getOfficeId());
                    } else {
                        throw new Error("Invalid transaction");
                    }
                })
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#01(b). archive: isValidWithdrawStagingData? {}", c.toString()))
                .flatMap(c -> {
                    if (c.equals(Boolean.TRUE)) {
                        return stagingDataArchiveService.archiveStagingDataIntoHistory(command.getOfficeId());
                    } else {
                        throw new Error("Invalid transaction");
                    }
                })
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#02. archive: archiveStagingDataIntoHistory? {}", c.toString()))
                .flatMap(c -> {
                    if (c.equals(Boolean.TRUE)) {
                        return stagingAccountDataArchiveService.archiveStagingAccountDataIntoHistory(command.getOfficeId());
                    } else {
                        throw new Error(c.toString());
                    }
                })
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#03. archive: archiveStagingAccountDataIntoHistory? {}", c.toString()))
                .flatMap(c -> {
                    if (c.equals(Boolean.TRUE)) {
                        return collectionStagingDataArchiveService.archiveCollectionStagingDataIntoHistory(command.getOfficeId());
                    } else {
                        throw new Error(c.toString());
                    }
                })
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#04. archive: archiveCollectionStagingDataIntoHistory? {}", c.toString()))
                .flatMap(c -> {
                    if (c.equals(Boolean.TRUE)) {
                        return withdrawStagingDataArchiveService.archiveWithdrawStagingDataIntoHistory(command.getOfficeId());
                    } else {
                        throw new Error(c.toString());
                    }
                })
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#05. archive: archiveWithdrawStagingDataIntoHistory? {}", c.toString()))
                .flatMap(c -> {
                    if (c.equals(Boolean.TRUE)) {
                        return withdrawStagingDataArchiveService.deleteLiveWithdrawStagingData(command.getOfficeId());
                    } else {
                        throw new Error(c.toString());
                    }
                })
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#06. archive: deleteLiveWithdrawStagingData? {}", c.toString()))
                .flatMap(c -> {
                    if (c.equals(Boolean.TRUE)) {
                        return collectionStagingDataArchiveService.deleteLiveCollectionStagingData(command.getOfficeId());
                    } else {
                        throw new Error(c.toString());
                    }
                })
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#07. archive: deleteLiveCollectionStagingData? {}", c.toString()))
                .flatMap(c -> {
                    if (c.equals(Boolean.TRUE)) {
                        return stagingAccountDataArchiveService.deleteLiveStagingAccountData(command.getOfficeId());
                    } else {
                        throw new Error(c.toString());
                    }
                })
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#08. archive: deleteLiveStagingAccountData? {}", c.toString()))
                .flatMap(c -> {
                    if (c.equals(Boolean.TRUE)) {
                        return stagingDataArchiveService.deleteLiveStagingData(command.getOfficeId());
                    } else {
                        throw new Error(c.toString());
                    }
                })
                .doOnError(error -> log.error(error.getMessage()))
                .doOnNext(c -> log.info("#09. archive: deleteLiveStagingData? {}", c.toString()))
                .as(this.rxtx::transactional)
                .doOnNext(c -> log.info("#10. archive: Transaction complete? {}", c))
                .map(c -> c.equals(Boolean.TRUE) ? "Data successfully archived." : "Data failed to archive.")
                .doOnNext(msg -> log.info("#11. archive: message? {}", msg))
                .map(msg -> DataArchiveResponseDto.builder().userMessage(msg).build())
                .doOnNext(res -> log.info("#12. archive: response? {}", res.toString()))
                .doOnSuccess(res -> reactiveValueOps.delete(getRedisKey(command.getOfficeId(), command.getMfiId())).subscribeOn(Schedulers.immediate()))
                .doOnNext(res -> log.info("#13. archive: response? {}", res.toString()));
    }

    private String getRedisKey(String officeId, String mfiId) {
        return Constants.STAGING_DATA_GENERATION_STATUS + "-" + mfiId + "-" + officeId;
    }
}
