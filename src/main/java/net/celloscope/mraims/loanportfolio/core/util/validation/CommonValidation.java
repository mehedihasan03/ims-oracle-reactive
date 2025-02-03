package net.celloscope.mraims.loanportfolio.core.util.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.core.util.validation.response.OfficeValidationResponseDTO;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity.CollectionStagingDataEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.out.entity.LoanAdjustmentDataEntity;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommonValidation {

    private final ManagementProcessTrackerUseCase managementProcessUseCase;
    private final OfficeEventTrackerUseCase officeEventUseCase;
    private final SamityEventTrackerUseCase samityEventUseCase;
    private final CommonRepository commonRepository;

    public Mono<OfficeValidationResponseDTO> getAndBuildOfficeValidationResponseDTO(String officeId){
        return managementProcessUseCase.getLastManagementProcessForOffice(officeId)
                .map(managementProcessTracker -> OfficeValidationResponseDTO.builder()
                        .managementProcessId(managementProcessTracker.getManagementProcessId())
                        .officeId(managementProcessTracker.getOfficeId())
                        .officeNameEn(managementProcessTracker.getOfficeNameEn())
                        .officeNameBn(managementProcessTracker.getOfficeNameBn())
                        .businessDate(managementProcessTracker.getBusinessDate())
                        .businessDay(managementProcessTracker.getBusinessDay())
                        .officeEventList(new ArrayList<>())
                        .isDayStarted(false)
                        .isStagingDataGenerated(false)
                        .isDayEndProcessCompleted(false)
                        .build())
                .flatMap(this::getOfficeEventListForOffice)
                .doOnNext(officeValidationDTO -> log.info("Office Validation DTO: {}", officeValidationDTO))
                .doOnError(throwable -> log.error("Error getting Office Validation DTO: {}", throwable.getMessage()));
    }

    private Mono<OfficeValidationResponseDTO> getOfficeEventListForOffice(OfficeValidationResponseDTO officeValidationResponseDTO){
        return officeEventUseCase.getAllOfficeEventsForOffice(officeValidationResponseDTO.getManagementProcessId(), officeValidationResponseDTO.getOfficeId())
                .map(OfficeEventTracker::getOfficeEvent)
                .doOnNext(officeEvent -> {
                    officeValidationResponseDTO.getOfficeEventList().add(officeEvent);
                    if(officeEvent.equals(OfficeEvents.DAY_STARTED.getValue())){
                        officeValidationResponseDTO.setIsDayStarted(true);
                        officeValidationResponseDTO.setUserMessage("Business Day Is Started For Office");
                    } else if(officeEvent.equals(OfficeEvents.STAGED.getValue())){
                        officeValidationResponseDTO.setIsStagingDataGenerated(true);
                        officeValidationResponseDTO.setUserMessage("Staging Data Is Generated For Office");
                    } else if(officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())){
                        officeValidationResponseDTO.setIsDayEndProcessCompleted(true);
                        officeValidationResponseDTO.setUserMessage("Day End Process Is Completed For Office");
                    }
                })
                .collectList()
                .map(officeEventList -> officeValidationResponseDTO);
    }

    public Mono<Boolean> checkIfCollectionDataAndAdjustmentDataNotExistsForLoanAccountId(String loanAccountId){
        return commonRepository.findFirstLoanAdjustmentDataByLoanAccountId(loanAccountId)
                .switchIfEmpty(Mono.just(LoanAdjustmentDataEntity.builder().build()))
                .zipWith(commonRepository.findFirstCollectionPaymentDataByLoanAccountId(loanAccountId)
                        .doOnNext(collectionStagingDataEntity -> log.info("Collection Data amount: {}", collectionStagingDataEntity.getAmount()))
                        .filter(collectionStagingDataEntity -> collectionStagingDataEntity.getAmount().compareTo(BigDecimal.ZERO) > 0)
                        .doOnNext(collectionStagingDataEntity -> log.info("Collection Data amount after filter: {}", collectionStagingDataEntity.getAmount()))
                        .switchIfEmpty(Mono.just(CollectionStagingDataEntity.builder().build())))
                            .map(loanAdjustmentDataAndCollectionStagingData -> {
                                log.info("Loan Adjustment Data for existence validation: {}", !HelperUtil.checkIfNullOrEmpty(loanAdjustmentDataAndCollectionStagingData.getT1().getLoanAccountId()));
                                log.info("Collection Payment Data for existence validation: {}", !HelperUtil.checkIfNullOrEmpty(loanAdjustmentDataAndCollectionStagingData.getT2().getLoanAccountId()) &&
                                        !loanAdjustmentDataAndCollectionStagingData.getT2().getAmount().equals(BigDecimal.ZERO));
                                if (!HelperUtil.checkIfNullOrEmpty(loanAdjustmentDataAndCollectionStagingData.getT1().getLoanAccountId()) ||
                                        (!HelperUtil.checkIfNullOrEmpty(loanAdjustmentDataAndCollectionStagingData.getT2().getLoanAccountId()) &&
                                                !loanAdjustmentDataAndCollectionStagingData.getT2().getAmount().equals(BigDecimal.ZERO)))
//                                    return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Collection Data Found For Loan Account Id"));
                                    return Boolean.FALSE;
                                return Boolean.TRUE;
                            });
    }

    public Mono<Boolean> checkIfCollectionDataNotExistsForSavingsAccountId(String savingsAccountId){
        return commonRepository.findFirstCollectionPaymentDataBySavingsAccountId(savingsAccountId)
                .switchIfEmpty(Mono.just(CollectionStagingDataEntity.builder().build()))
                .map(collectionStagingDataEntity -> {
                    if (!HelperUtil.checkIfNullOrEmpty(collectionStagingDataEntity.getSavingsAccountId()) &&
                                    !collectionStagingDataEntity.getAmount().equals(BigDecimal.ZERO))
                        return Boolean.FALSE;
                    return Boolean.TRUE;
                });
    }
}
