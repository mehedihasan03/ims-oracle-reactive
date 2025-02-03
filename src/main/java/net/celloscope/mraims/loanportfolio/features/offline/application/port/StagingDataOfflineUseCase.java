package net.celloscope.mraims.loanportfolio.features.offline.application.port;

import net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.request.StagingDataOfflineRequestDTO;
import net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.response.StagingDataDownloadByFieldOfficerResponseDTO;
import net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.response.StagingDataOfflineResponseDTO;
import reactor.core.publisher.Mono;

public interface StagingDataOfflineUseCase {

    Mono<StagingDataDownloadByFieldOfficerResponseDTO> downloadStagingDataByFieldOfficer(StagingDataOfflineRequestDTO requestDTO);
    Mono<StagingDataOfflineResponseDTO> deleteDownloadedStagingDataByFieldOfficer(StagingDataOfflineRequestDTO requestDTO);
}
