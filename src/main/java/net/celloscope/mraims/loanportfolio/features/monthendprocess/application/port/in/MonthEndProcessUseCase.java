package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in;

import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.request.MonthEndProcessRequestDTO;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response.MonthEndProcessAccountingViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response.MonthEndProcessGridViewResponseDTO;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response.MonthEndProcessResponseDTO;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response.MonthEndProcessStatusGridViewResponseDTO;
import reactor.core.publisher.Mono;

public interface MonthEndProcessUseCase {

    Mono<MonthEndProcessGridViewResponseDTO> gridViewOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO);
    Mono<MonthEndProcessStatusGridViewResponseDTO> gridViewOfStatusOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO);
    Mono<MonthEndProcessStatusGridViewResponseDTO> runSamityStatusOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO);
    Mono<MonthEndProcessStatusGridViewResponseDTO> retrySamityStatusOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO);
    Mono<MonthEndProcessStatusGridViewResponseDTO> retryAllSamityStatusOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO);
    Mono<MonthEndProcessAccountingViewResponseDTO> gridViewOfAccountingOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO);
    Mono<MonthEndProcessAccountingViewResponseDTO> runAccountingOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO);
    Mono<MonthEndProcessAccountingViewResponseDTO> retryAccountingByTransactionCodeListOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO);
    Mono<MonthEndProcessAccountingViewResponseDTO> retryAllAccountingOfMonthEndProcess(MonthEndProcessRequestDTO requestDTO);
    Mono<MonthEndProcessResponseDTO> revertMonthEndProcess(MonthEndProcessRequestDTO requestDTO);

}
