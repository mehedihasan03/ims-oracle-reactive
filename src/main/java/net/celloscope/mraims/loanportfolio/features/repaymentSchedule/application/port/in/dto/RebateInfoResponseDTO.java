package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class RebateInfoResponseDTO {
    private BigDecimal totalPayable;
    private BigDecimal totalPrincipal;
    private BigDecimal totalServiceCharge;
}
