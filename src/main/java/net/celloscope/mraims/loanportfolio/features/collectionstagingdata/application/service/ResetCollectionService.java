package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.SamityEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.ResetCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.ResetCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.ResetCollectionResponseDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain.CollectionStagingData;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetCollectionService implements ResetCollectionUseCase {
    private final CollectionStagingDataPersistencePort port;
    private final CollectionStagingDataQueryUseCase collectionStagingDataQueryUseCase;

    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;

    private final TransactionalOperator rxtx;

    @Override
    public Mono<ResetCollectionResponseDto> resetCollection(ResetCollectionCommand command) {
        return officeEventTrackerUseCase.getManagementProcessIdOfLastStagedOfficeEventForOffice(command.getOfficeId())
                .flatMap(managementProcessId ->
                        port.getAllCollectionStagingDataByManagementProcessIdAndSamityId(
                                        managementProcessId, command.getSamityId())
                                .filter(collectionStagingData ->
                                        Stream.of(
                                                Status.STATUS_STAGED.getValue(),
                                                Status.STATUS_REJECTED.getValue()
                                        ).anyMatch(collectionStagingDataStatus ->
                                                collectionStagingData.getStatus()
                                                        .trim().equalsIgnoreCase(collectionStagingDataStatus)))
                                .switchIfEmpty(
                                        Mono.error(new ExceptionHandlerUtil(
                                                HttpStatus.INTERNAL_SERVER_ERROR,
                                                "An error occurred while trying to reset Collection")))
                                .filter(collectionStagingData -> collectionStagingData.getCollectionType().equalsIgnoreCase("Regular") && collectionStagingData.getCreatedBy().equals(command.getLoginId()))
                                .collectList()
                                .flatMap(data -> port.saveAllCollectionStagingDataIntoEditHistory(data, command.getLoginId()))
                                .flatMap(collectionStagingData ->  port.deleteAllCollectionStagingData(collectionStagingData)
                                        .then(Mono.defer(() ->  collectionStagingDataQueryUseCase.countCollectionStagingData(collectionStagingData.get(0).getManagementProcessId(), collectionStagingData.get(0).getSamityId())))
                                        .doOnNext(count -> log.info("Count of Collection Staging Data: {}", count))
                                        .flatMap(count -> {
                                            if (count == 0) {
                                                return samityEventTrackerUseCase.getLastCollectedOrCancelledSamityEventBySamityAndManagementProcessId(command.getSamityId(), managementProcessId)
                                                        .flatMap(data -> samityEventTrackerUseCase.saveSamityEventTrackerIntoHistoryAndDeleteLiveData(data, command.getLoginId()));
                                            }
                                            return Mono.empty();
                                        }))
                )
                .as(this.rxtx::transactional)
                .then(Mono.just("Collection was reset successfully"))
                .doOnSuccess(log::info)
                .doOnError(e -> log.error("Error occurred while trying to reset collection: {}", e.getMessage()))
                .mapNotNull(ResetCollectionResponseDto::new);
    }

    @Override
    public Mono<ResetCollectionResponseDto> resetSpecialCollection(String oid) {
        return port.getCollectionStagingDataByOid(oid)
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.NOT_FOUND, "No collection data found for oid: " + oid)))
                .filter(collectionStagingData -> collectionStagingData.getStatus().equalsIgnoreCase(Status.STATUS_STAGED.getValue()) || collectionStagingData.getStatus().equalsIgnoreCase(Status.STATUS_REJECTED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection data is not eligible to reset!")))
                .flatMap(collectionStagingData -> port.deleteSpecialCollectionStagingDataByOid(oid)
                    .then(port.getAllCollectionDataBySamityId(collectionStagingData.getSamityId())
                        .filter(List::isEmpty)
                        .flatMap(collectionStagingDataList-> samityEventTrackerUseCase
                            .deleteSamityEventTrackerByEventList(collectionStagingData.getManagementProcessId(), collectionStagingData.getSamityId(), List.of(SamityEvents.COLLECTED.getValue())))))
                .as(this.rxtx::transactional)
                .doOnSuccess(strings -> log.info("Special Collection was reset successfully"))
                .thenReturn(ResetCollectionResponseDto
                .builder()
                .userMessage("Special Collection was reset successfully")
                .build())
                .doOnError(e -> log.error("Error occurred while trying to reset special collection: {}", e.getMessage()));

    }
}
