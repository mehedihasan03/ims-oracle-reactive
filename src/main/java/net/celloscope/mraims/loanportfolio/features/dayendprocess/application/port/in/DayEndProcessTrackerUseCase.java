package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in;

import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.request.DayEndProcessRequestDTO;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.*;
import reactor.core.publisher.Mono;

public interface DayEndProcessTrackerUseCase {

    Mono<DayEndProcessGridViewResponseDTO> gridViewOfDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessDetailViewResponseDTO> detailViewOfDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessDetailViewResponseDTO> runDayEndProcessForOfficeV1(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessRetryResponseDTO> retryDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessRetryResponseDTO> retryAllDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessResponseDTO> generateAutoVoucherForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessResponseDTO> retryAutoVoucherGenerationForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessResponseDTO> deleteAutoVoucherGenerationForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessResponseDTO> runDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessResponseDTO> deleteDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessResponseDTO> revertDayEndProcessByAISForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessResponseDTO> revertDayEndProcessByMISForOffice(DayEndProcessRequestDTO requestDTO);
    Mono<DayEndProcessStatusResponseDTO> getStatusOfDayEndProcessForOffice(DayEndProcessRequestDTO requestDTO);
}
