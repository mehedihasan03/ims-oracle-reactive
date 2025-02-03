package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in;

import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.request.ProcessTrackerRequestDTO;
import net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.response.*;
import reactor.core.publisher.Mono;

public interface ProcessManagementUseCase {
	
	Mono<DayEndProcessResponseDTO> runDayEndProcessForOffice(ProcessTrackerRequestDTO requestDTO);

	Mono<ProcessTrackerResponseDTO> runMonthEndProcessForOffice(ProcessTrackerRequestDTO requestDTO);
	
	Mono<ProcessTrackerResponseDTO> runForwardDayRoutineForOffice(ProcessTrackerRequestDTO requestDTO);
	
	Mono<ProcessDashboardOfOfficeResponseDTO> gridViewOfOfficeProcessDashboardForMfi(ProcessTrackerRequestDTO requestDTO);
	
	Mono<ProcessDashboardOfSamityResponseDTO> gridViewOfSamityProcessDashboardForOffice(ProcessTrackerRequestDTO requestDTO);

	Mono<ProcessTrackerResponseDTO> getCurrentProcessManagementForOffice(ProcessTrackerRequestDTO requestDTO);

//	Process Management V2

	Mono<ForwardDayGridViewResponseDTO> gridViewOfForwardDayRoutineForOffice(ProcessTrackerRequestDTO requestDTO);
	Mono<SamityCancelGridViewResponseDTO> gridViewOfSamityCancelForOffice(ProcessTrackerRequestDTO requestDTO);
	Mono<ProcessTrackerResponseDTO> cancelRegularSamityListForOffice(ProcessTrackerRequestDTO requestDTO);
	Mono<ProcessTrackerResponseDTO> deleteRegularSamityListCancellationForOffice(ProcessTrackerRequestDTO requestDTO);
	Mono<ProcessTrackerResponseDTO> runForwardDayRoutineForOfficeV2(ProcessTrackerRequestDTO requestDTO);
	Mono<ProcessTrackerResponseDTO> revertForwardDayRoutineForOfficeV2(ProcessTrackerRequestDTO requestDTO);
	Mono<ProcessTrackerResponseDTO> getCreateTransactionButtonStatusForOffice(ProcessTrackerRequestDTO requestDTO);
}
