package net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleViewDTO;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanCalculatorResponseDTO {
    private BigDecimal calculatedInstallmentAmount;
    private BigDecimal selectedInstallmentAmount;
    private Integer installmentRoundedToNextInteger;
    private BigDecimal lastInstallmentAmount;
    private BigDecimal serviceChargeRatePerPeriod;
    private BigDecimal totalServiceCharge;
    private List<RepaymentScheduleViewDTO> data;
    private String userMessage;
}
