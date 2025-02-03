package net.celloscope.mraims.loanportfolio.features.offline.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.offline.application.port.StagingDataOfflineUseCase;
import net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.request.StagingDataOfflineRequestDTO;
import net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.response.StagingDataDownloadByFieldOfficerResponseDTO;
import net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.response.StagingDataOfflineResponseDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.ManagementProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.OfficeEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.SamityEventTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.IStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request.StagingDataRequestDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.IWithdrawStagingDataUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class StagingDataOfflineService implements StagingDataOfflineUseCase {

    private final ManagementProcessTrackerUseCase managementProcessTrackerUseCase;
    private final OfficeEventTrackerUseCase officeEventTrackerUseCase;
    private final SamityEventTrackerUseCase samityEventTrackerUseCase;

    private final IStagingDataUseCase stagingDataUseCase;
    private final PaymentCollectionUseCase collectionUseCase;
    private final IWithdrawStagingDataUseCase withdrawUseCase;
    private final LoanAdjustmentUseCase loanAdjustmentUseCase;

    private final Gson gson;

    public StagingDataOfflineService(ManagementProcessTrackerUseCase managementProcessTrackerUseCase, OfficeEventTrackerUseCase officeEventTrackerUseCase, SamityEventTrackerUseCase samityEventTrackerUseCase, IStagingDataUseCase stagingDataUseCase, PaymentCollectionUseCase collectionUseCase, IWithdrawStagingDataUseCase withdrawUseCase, LoanAdjustmentUseCase loanAdjustmentUseCase) {
        this.managementProcessTrackerUseCase = managementProcessTrackerUseCase;
        this.officeEventTrackerUseCase = officeEventTrackerUseCase;
        this.samityEventTrackerUseCase = samityEventTrackerUseCase;
        this.stagingDataUseCase = stagingDataUseCase;
        this.collectionUseCase = collectionUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.loanAdjustmentUseCase = loanAdjustmentUseCase;
        this.gson = CommonFunctions.buildGson(this);
    }

    @Override
    public Mono<StagingDataDownloadByFieldOfficerResponseDTO> downloadStagingDataByFieldOfficer(StagingDataOfflineRequestDTO requestDTO) {
        return stagingDataUseCase.downloadStagingDataByFieldOfficer(gson.fromJson(requestDTO.toString(), StagingDataRequestDTO.class))
                .map(response -> gson.fromJson(response.toString(), StagingDataDownloadByFieldOfficerResponseDTO.class));
    }

    @Override
    public Mono<StagingDataOfflineResponseDTO> deleteDownloadedStagingDataByFieldOfficer(StagingDataOfflineRequestDTO requestDTO) {
        return stagingDataUseCase.deleteStagingDataByFieldOfficer(gson.fromJson(requestDTO.toString(), StagingDataRequestDTO.class))
                .map(response -> gson.fromJson(response.toString(), StagingDataOfflineResponseDTO.class));
    }
}
