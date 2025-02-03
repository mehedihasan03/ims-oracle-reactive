package net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in;

import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.request.LoanCalculatorRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.response.LoanCalculatorResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.response.LoanProductInfoResponseDTO;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.response.LoanProductListResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleViewDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface LoanCalculatorUseCase {
    Mono<LoanProductListResponseDTO> getActiveLoanProductsByMfi(String instituteOid);
    Mono<LoanProductInfoResponseDTO> getLoanProductInfo(String loanProductId);
    Mono<LoanCalculatorResponseDTO> generateRepaymentScheduleForLoan(LoanCalculatorRequestDTO requestDTO);
}
