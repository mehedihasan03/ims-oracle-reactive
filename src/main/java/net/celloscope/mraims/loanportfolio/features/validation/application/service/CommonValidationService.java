package net.celloscope.mraims.loanportfolio.features.validation.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.ExceptionMessages;
import net.celloscope.mraims.loanportfolio.core.util.enums.OfficeEvents;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;
import net.celloscope.mraims.loanportfolio.features.validation.application.port.in.ICommonValidationUseCase;
import net.celloscope.mraims.loanportfolio.features.validation.application.port.in.dto.OfficeValidationDTO;
import net.celloscope.mraims.loanportfolio.features.validation.application.port.in.dto.SamityValidationDTO;
import net.celloscope.mraims.loanportfolio.features.validation.application.port.out.ICommonValidationGatewayPort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
public class CommonValidationService implements ICommonValidationUseCase {
    private final ICommonValidationGatewayPort gatewayPort;
    private final Gson gson;

    public CommonValidationService(ICommonValidationGatewayPort gatewayPort) {
        this.gatewayPort = gatewayPort;
        this.gson = CommonFunctions.buildGson(this);
    }


    @Override
    public Mono<ManagementProcessTracker> validateStagingDataGenerationRequestForOffice(String officeId) {
        log.info("Staging Data Generation Process Validation is Started for Office: {}", officeId);
        return this.getAndBuildOfficeValidationDTOForStagingData(officeId)
                .filter(validationDTO -> validationDTO.getOfficeEvents().stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.STAGING_DATA_IS_ALREADY_GENERATED_FOR_OFFICE.getValue())))
                .filter(validationDTO -> validationDTO.getSamityIdList().isEmpty())
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.STAGING_DATA_GENERATION_PROCESS_IS_RUNNING_FOR_OFFICE.getValue())))
                .doOnNext(validationDTO -> log.info("Staging Data Generation Process Validation is Successful for Office: {}", officeId))
                .map(OfficeValidationDTO::getManagementProcessTracker);
    }

    @Override
    public Mono<Boolean> validateSamityStagingDataInvalidationRequestForSamityList(String officeId, List<String> samityIdList) {
        log.info("Samity Staging Data Invalidate Process Validation is Started for Samity: {}", samityIdList);
        return this.getAndBuildOfficeValidationDTOForStagingData(officeId)
                .filter(validationDTO -> validationDTO.getOfficeEvents().stream().anyMatch(officeEvent -> officeEvent.equals(OfficeEvents.STAGING_DATA_GENERATION_COMPLETED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.STAGING_DATA_IS_NOT_GENERATED_FOR_OFFICE.getValue())))
                .filter(validationDTO -> validationDTO.getOfficeEvents().stream().noneMatch(officeEvent -> officeEvent.equals(OfficeEvents.DAY_END_PROCESS_COMPLETED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.DAY_END_PROCESS_IS_ALREADY_COMPLETED_FOR_OFFICE.getValue())))
                .filter(validationDTO -> !samityIdList.isEmpty() && !validationDTO.getSamityIdList().isEmpty() && new HashSet<>(validationDTO.getSamityIdList()).containsAll(samityIdList))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.SAMITY_LIST_IS_NOT_VALID_FOR_STAGING_DATA_INVALIDATION.getValue())))
                .flatMap(validationDTO -> this.getAndBuildSamityStagingDataValidationDTOList(validationDTO.getManagementProcessId(), samityIdList))
                .filter(samityValidationList -> samityValidationList.stream().allMatch(samityValidationDTO -> samityValidationDTO.getStatus().equals(Status.STATUS_FINISHED.getValue())))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.SAMITY_STAGING_DATA_GENERATION_IS_NOT_FINISHED.getValue())))
                .filter(samityValidationList -> samityValidationList.stream().noneMatch(samityValidationDTO -> samityValidationDTO.getIsDownloaded().equals("Yes")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.SAMITY_STAGING_DATA_IS_ALREADY_DOWNLOADED.getValue())))
                .filter(samityValidationList -> samityValidationList.stream().allMatch(samityValidationDTO -> samityValidationDTO.getSamityEvents().isEmpty()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, ExceptionMessages.SAMITY_EVENT_FOUND.getValue())))
                .doOnNext(samityValidationList -> log.info("Samity Staging Data Invalidate Process Validation is Successful for Samity: {}", samityIdList))
                .map(validationDTO -> true);
    }

    private Mono<List<SamityValidationDTO>> getAndBuildSamityStagingDataValidationDTOList(String managementProcessId, List<String> samityIdList) {
        return gatewayPort.getAndBuildSamityValidationDTOList(managementProcessId, samityIdList)
                .doOnNext(samityValidationList -> log.info("Samity Staging Data Validation List: {}", samityValidationList));
    }

    private Mono<OfficeValidationDTO> getAndBuildOfficeValidationDTOForStagingData(String officeId){
        return this.getAndBuildOfficeValidationDTO(officeId)
                .flatMap(validationDTO -> this.getStagingProcessTrackerSamityListForOffice(validationDTO.getManagementProcessId(), officeId)
                        .doOnNext(validationDTO::setSamityIdList)
                        .map(samityIdList -> validationDTO))
                .doOnNext(validationDTO -> log.info("Staging Data Generation Validation DTO: {}", validationDTO));
    }

    private Mono<OfficeValidationDTO> getAndBuildOfficeValidationDTO(String officeId){
        return gatewayPort.getAndBuildOfficeValidationDTO(officeId);
    }

    private Mono<List<String>> getStagingProcessTrackerSamityListForOffice(String managementProcessId, String officeId){
        return gatewayPort.getStagingProcessTrackerSamityListForOffice(managementProcessId, officeId);
    }
}
