package net.celloscope.mraims.loanportfolio.features.archive.application.port.in;

import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.dto.DataArchiveRequestDTO;
import net.celloscope.mraims.loanportfolio.features.archive.application.port.in.dto.DataArchiveResponseDTO;
import reactor.core.publisher.Mono;

public interface IDataArchiveUseCase {

    Mono<DataArchiveResponseDTO> archiveAndDeleteStagingDataForOffice(DataArchiveRequestDTO requestDTO);

    Mono<String> saveLoanAdjustmentIntoHistory(String managementProcessId, String processId);
    Mono<String> revertArchiveDataAndDeleteHistoryForDayForwardRoutine(String managementProcessId);

}
