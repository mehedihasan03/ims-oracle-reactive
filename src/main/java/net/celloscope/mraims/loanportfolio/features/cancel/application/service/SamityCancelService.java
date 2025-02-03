package net.celloscope.mraims.loanportfolio.features.cancel.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.SamityEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.CancelledSamityUseCase;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.request.CancelSamityRequestDTO;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.response.CancelSamityResponseDTO;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.out.persistence.ICancelSamityPersistencePort;
import net.celloscope.mraims.loanportfolio.features.cancel.domain.CancelSamity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.ManagementProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.Samity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.Messages.RESTORE_CANCELED_SAMITY;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.SAMITY_CANCELED;
import static net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages.SOMETHING_WENT_WRONG;

@Service
@Slf4j
@RequiredArgsConstructor
public class SamityCancelService implements CancelledSamityUseCase {

    private final CommonRepository commonRepository;
    private final ICancelSamityPersistencePort persistencePort;
    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;

   /* public SamityCancelService(CommonRepository commonRepository, ICancelSamityPersistencePort persistencePort) {
        this.commonRepository = commonRepository;
        this.persistencePort = persistencePort;
    }*/

    @Override
    public Mono<CancelSamityResponseDTO> cancelSamityBySamityId(CancelSamityRequestDTO requestDTO) {
        /*
         * get businessDay
         * get samityDay
         * check if samityDay == businessDay
         *   true -> check if samityEvent exist
         *           true -> return error ("Samity cannot be cancelled")
         *           false -> cancel samity (insert into samity event tracker)
         * */
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .map(managementProcessTracker -> ManagementProcessTrackerEntity.builder()
                        .managementProcessId(managementProcessTracker.getManagementProcessId())
                        .officeId(managementProcessTracker.getOfficeId())
                        .businessDate(managementProcessTracker.getBusinessDate())
                        .businessDay(managementProcessTracker.getBusinessDay())
                        .build())
                .zipWith(commonRepository.getSamityBySamityId(requestDTO.getSamityId()))
                .flatMap(tupleOfSamityAndManagementTracker -> {
                    ManagementProcessTrackerEntity managementProcessTracker = tupleOfSamityAndManagementTracker.getT1();
                    Samity samity= tupleOfSamityAndManagementTracker.getT2();
                    requestDTO.setManagementProcessId(managementProcessTracker.getManagementProcessId());
                    if (managementProcessTracker.getBusinessDay().equals(samity.getSamityDay())) {
                        log.info("Business day and Samity day are same");
                        return commonRepository
                                .checkIfSamityEventExists(samity.getSamityId(), managementProcessTracker.getManagementProcessId())
                                .map(aBoolean -> !aBoolean);
                    } else {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Special samity with id: " + requestDTO.getSamityId() + " cannot be canceled"));
                    }
                })
                .flatMap(aBoolean -> aBoolean
                        ? persistencePort.saveSamityEventTracker(buildDomain(requestDTO))
                        : Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Regular Samity with id: " + requestDTO.getSamityId() + " cannot be Canceled.")))
                .map(samityEventTrackerEntity -> CancelSamityResponseDTO
                        .builder()
                        .userMessage(SAMITY_CANCELED.getValue() + samityEventTrackerEntity.getSamityId())
                        .build());
    }

    @Override
    public Mono<CancelSamityResponseDTO> restoreCancelledSamityBySamityId(CancelSamityRequestDTO requestDTO) {
        log.debug("Restoring cancelled samity with id: {}", requestDTO.getSamityId());
        return managementProcessTrackerUseCase.getLastManagementProcessForOffice(requestDTO.getOfficeId())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Office with id: " + requestDTO.getOfficeId() + " cannot be restored")))
                .flatMap(managementProcessTracker -> commonRepository.getSamityBySamityId(requestDTO.getSamityId())
                        .filter(samity -> samity.getSamityDay().equalsIgnoreCase(managementProcessTracker.getBusinessDay()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with id: " + requestDTO.getSamityId() + " cannot be restored")))
                        .thenReturn(managementProcessTracker)
                )
                .flatMap(managementProcessTracker -> samityEventTrackerUseCase.getLastCollectedOrCancelledSamityEventBySamityAndManagementProcessId(requestDTO.getSamityId(), managementProcessTracker.getManagementProcessId()))
                .filter(samityEventTrackerEntity -> samityEventTrackerEntity.getSamityEvent().equals(SamityEvents.CANCELED.getValue()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with id: " + requestDTO.getSamityId() + " is not cancelled")))
                .flatMap(samityEventTracker -> officeEventTrackerUseCase.getLastOfficeEventForOffice(samityEventTracker.getManagementProcessId(), requestDTO.getOfficeId())
                        .filter(officeEventTrackerEntity -> officeEventTrackerEntity.getOfficeEvent().equals(OfficeEvents.STAGED.getValue()))
                        .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Samity with id: " + requestDTO.getSamityId() + " cannot be restored")))
                        .flatMap(officeEventTrackerEntity -> samityEventTrackerUseCase.deleteSamityEventTrackerByEventTrackerIdList(List.of(samityEventTracker.getSamityEventTrackerId())))
                        .doOnSuccess(samityEventTrackerIdList -> log.info("Samity event tracker deleted successfully for canceled samity: {}", samityEventTrackerIdList))
                        .thenReturn(RESTORE_CANCELED_SAMITY)
                )
                .flatMap(userMsg -> Mono.just(CancelSamityResponseDTO.builder().userMessage(userMsg).build()))
                .doOnError(err -> log.error("Error occurred while restoring cancelled samity : {}", err.getMessage()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> Mono.error(new ExceptionHandlerUtil(HttpStatus.INTERNAL_SERVER_ERROR, SOMETHING_WENT_WRONG.getValue())));

    }

    private CancelSamity buildDomain(CancelSamityRequestDTO requestDTO) {
        return CancelSamity
                .builder()
                .samityId(requestDTO.getSamityId())
                .remarks(requestDTO.getRemarks())
                .samityEventTrackerId(UUID.randomUUID().toString())
                .managementProcessId(requestDTO.getManagementProcessId())
                .createdOn(LocalDateTime.now())
                .createdBy(requestDTO.getLoginId())
                .samityEvent(Status.STATUS_CANCELLED.getValue())
                .officeId(requestDTO.getOfficeId())
                .build();
    }
}
