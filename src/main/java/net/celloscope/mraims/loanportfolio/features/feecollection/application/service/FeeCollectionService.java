package net.celloscope.mraims.loanportfolio.features.feecollection.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.FeeCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.dto.request.FeeCollectionUpdateRequestDTO;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.out.FeeCollectionPersistencePort;
import net.celloscope.mraims.loanportfolio.features.feecollection.application.port.out.FeeTypeSettingPersistencePort;
import net.celloscope.mraims.loanportfolio.features.feecollection.domain.FeeCollection;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.ui.ModelMap;
import org.testng.util.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class FeeCollectionService implements FeeCollectionUseCase {
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final FeeCollectionPersistencePort feeCollectionPersistencePort;
    private final FeeTypeSettingPersistencePort feeTypeSettingPersistencePort;
    private final TransactionalOperator rxtx;
    private final ModelMapper modelMapper;

    public FeeCollectionService(ManagementProcessTrackerUseCase managementProcessTrackerUseCase, FeeCollectionPersistencePort feeCollectionPersistencePort, FeeTypeSettingPersistencePort feeTypeSettingPersistencePort, TransactionalOperator rxtx, ModelMapper modelMapper) {
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.feeCollectionPersistencePort = feeCollectionPersistencePort;
        this.feeTypeSettingPersistencePort = feeTypeSettingPersistencePort;
        this.rxtx = rxtx;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<List<FeeCollection>> updateNullableFeeCollectionByOfficeId(FeeCollectionUpdateRequestDTO requestDto) {
        AtomicReference<ManagementProcessTracker> managementProcess = new AtomicReference<>();
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDto.getOfficeId())
                .doOnNext(managementProcess::set)
                .flatMapMany(managementProcessTracker -> this.getFeeCollectionByOfficeIdWhereManagementProcessIdIsNull(requestDto.getOfficeId())
                        .flatMap(feeCollection -> feeTypeSettingPersistencePort.findByFeeSettingId(feeCollection.getFeeTypeSettingId(), requestDto.getOfficeId())
                                .flatMap(feeTypeSetting -> {
                                    if (Strings.isNullOrEmpty(feeTypeSetting.getLedgerId())) {
                                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Ledger Id not found for Fee Type Setting Id: " + feeCollection.getFeeTypeSettingId()));
                                    }

                                    feeCollection.setManagementProcessId(managementProcess.get().getManagementProcessId());
                                    feeCollection.setCreditLedgerId(feeTypeSetting.getLedgerId());
                                    feeCollection.setCreditSubledgerId(feeTypeSetting.getSubledgerId());
                                    feeCollection.setOfficeId(requestDto.getOfficeId());
                                    feeCollection.setStatus(Status.STATUS_APPROVED.getValue());
                                    return Mono.just(feeCollection);
                                })
                        )
                )
                .collectList()
                .flatMap(feeCollections ->
                        feeCollectionPersistencePort.saveAll(feeCollections)
                                .then(Mono.just(feeCollections))
                )
                .as(rxtx::transactional)
                .doOnNext(feeCollections -> log.info("Fee Collection Updated Successfully"))
                .doOnError(throwable -> log.error("Error while updating Fee Collection", throwable));
    }

    @Override
    public Flux<FeeCollection> getFeeCollectionByOfficeId(String officeId) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                        .map(ManagementProcessTracker::getManagementProcessId)
                        .flatMapMany(managementProcessId -> feeCollectionPersistencePort.getFeeCollectionByOfficeIdAndManagementProcessId(officeId, managementProcessId));
    }

    @Override
    public Flux<FeeCollection> getFeeCollectionByOfficeIdForCurrentDay(String officeId) {
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(officeId)
                .map(ManagementProcessTracker::getManagementProcessId)
                .flatMapMany(managementProcessId -> feeCollectionPersistencePort.getFeeCollectionByOfficeIdAndManagementProcessIdOrManagementProcessIdIsNull(officeId, managementProcessId));
    }

    @Override
    public Mono<Boolean> updateFeeCollectionStatusByManagementProcessId(String officeId, String managementProcessId, String status) {
        return feeCollectionPersistencePort.updateFeeCollectionStatusByManagementProcessId(officeId, managementProcessId, status)
                .doOnNext(aBoolean -> log.info("Fee Collection Status Updated Successfully"))
                .doOnError(throwable -> log.error("Error while updating Fee Collection Status : {}", throwable.getMessage()));
    }

    @Override
    public Mono<List<FeeCollection>> rollbackFeeCollectionOnMISDayEndRevert(String officeId, String managementProcessId) {
        return feeCollectionPersistencePort.getFeeCollectionByOfficeIdAndManagementProcessId(officeId, managementProcessId)
                .collectList()
                .flatMap(feeCollections -> {
                    if (feeCollections.isEmpty()) {
                        return Mono.just(List.of(FeeCollection.builder().build()));
                    }
                    return feeCollectionPersistencePort.rollbackFeeCollectionOnMISDayEndRevert(officeId, managementProcessId);
                })
                .doOnError(throwable -> log.error("Error while rollback Fee Collection", throwable));
    }

    Flux<FeeCollection> getFeeCollectionByOfficeIdWhereManagementProcessIdIsNull(String officeId) {
        return feeCollectionPersistencePort.getFeeCollectionByOfficeIdAndManagementProcessId(officeId, null)
                .filter(feeCollection -> HelperUtil.checkIfNullOrEmpty(feeCollection.getManagementProcessId()))
                /*.switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Fee Collection Data not found")))*/
                ;
    }
}
