package net.celloscope.mraims.loanportfolio.features.validation.adapter.out;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.CollectionStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.out.gateway.WithdrawStagingDataPersistencePort;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.OfficeEventTracker;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.SamityEventTracker;
import net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity.StagingProcessTrackerEntity;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.out.persistence.IStagingProcessTrackerPersistencePort;
import net.celloscope.mraims.loanportfolio.features.validation.application.port.in.dto.OfficeValidationDTO;
import net.celloscope.mraims.loanportfolio.features.validation.application.port.in.dto.SamityValidationDTO;
import net.celloscope.mraims.loanportfolio.features.validation.application.port.out.ICommonValidationGatewayPort;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.IWithdrawStagingDataUseCase;
import org.springframework.stereotype.Component;
import org.testng.util.Strings;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class CommonValidationGatewayAdapter implements ICommonValidationGatewayPort {
    private final ManagementProcessTrackerUseCase managementProcessUseCase;
    private final OfficeEventTrackerUseCase officeEventUseCase;
    private final SamityEventTrackerUseCase samityEventUseCase;
    private final IStagingProcessTrackerPersistencePort stagingProcessPort;
    private final CollectionStagingDataPersistencePort collectionPort;
    private final WithdrawStagingDataPersistencePort withdrawPort;
    private final Gson gson;

    public CommonValidationGatewayAdapter(ManagementProcessTrackerUseCase managementProcessUseCase, OfficeEventTrackerUseCase officeEventUseCase, SamityEventTrackerUseCase samityEventUseCase, IStagingProcessTrackerPersistencePort stagingProcessPort, CollectionStagingDataPersistencePort collectionPort, WithdrawStagingDataPersistencePort withdrawPort) {
        this.managementProcessUseCase = managementProcessUseCase;
        this.officeEventUseCase = officeEventUseCase;
        this.samityEventUseCase = samityEventUseCase;
        this.stagingProcessPort = stagingProcessPort;
        this.collectionPort = collectionPort;
        this.withdrawPort = withdrawPort;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<OfficeValidationDTO> getAndBuildOfficeValidationDTO(String officeId) {
        return managementProcessUseCase.getLastManagementProcessForOffice(officeId)
                .filter(managementProcessTracker -> Strings.isNotNullAndNotEmpty(managementProcessTracker.getManagementProcessId()))
                .switchIfEmpty(Mono.error(new Exception("No Management Process Id found for Office: " + officeId)))
                .map(managementProcessTracker -> {
                    OfficeValidationDTO officeValidationDTO = gson.fromJson(managementProcessTracker.toString(), OfficeValidationDTO.class);
                    officeValidationDTO.setManagementProcessTracker(managementProcessTracker);
                    return officeValidationDTO;
                })
                .flatMap(validationDTO -> officeEventUseCase.getAllOfficeEventsForOffice(validationDTO.getManagementProcessId(), officeId)
                        .filter(officeEventTracker -> !HelperUtil.checkIfNullOrEmpty(officeEventTracker.getOfficeEvent()))
                        .map(OfficeEventTracker::getOfficeEvent)
                        .collectList()
                        .map(officeEvents -> {
                            validationDTO.setOfficeEvents(officeEvents);
                            return validationDTO;
                        }));
    }

    @Override
    public Mono<List<String>> getStagingProcessTrackerSamityListForOffice(String managementProcessId, String officeId) {
        return stagingProcessPort.getStagingProcessEntityByOffice(managementProcessId, officeId)
                .filter(stagingProcessTrackerEntity -> !HelperUtil.checkIfNullOrEmpty(stagingProcessTrackerEntity.getSamityId()))
                .map(StagingProcessTrackerEntity::getSamityId)
                .collectList();
    }

    @Override
    public Mono<List<SamityValidationDTO>> getAndBuildSamityValidationDTOList(String managementProcessId, List<String> samityIdList) {
        return stagingProcessPort.getStagingProcessEntityListBySamityIdList(managementProcessId, samityIdList)
                .filter(stagingProcessTrackerEntity -> !HelperUtil.checkIfNullOrEmpty(stagingProcessTrackerEntity.getSamityId()))
                .map(stagingProcessTrackerEntity -> gson.fromJson(stagingProcessTrackerEntity.toString(), SamityValidationDTO.class))
                .flatMap(samityValidationDTO -> samityEventUseCase.getAllSamityEventsForSamity(managementProcessId, samityValidationDTO.getSamityId())
                        .filter(samityEventTracker -> !HelperUtil.checkIfNullOrEmpty(samityEventTracker.getSamityEvent()))
                        .map(SamityEventTracker::getSamityEvent)
                        .collectList()
                        .doOnNext(samityValidationDTO::setSamityEvents)
                        .map(samityEvents -> samityValidationDTO))
                .collectList();
    }
}
