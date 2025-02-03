package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanRebateResponseDTO {

    private BigDecimal totalLoanPayable;
    private BigDecimal totalPrincipal;
    private BigDecimal totalServiceCharge;

    private BigDecimal totalTransaction;
    private BigDecimal totalPrincipalPaid;
    private BigDecimal totalServiceChargePaid;

    private BigDecimal totalOutstandingAmount;
    private BigDecimal totalPrincipalRemaining;
    private BigDecimal totalServiceChargeRemaining;

    private BigDecimal rebateAbleAmount;
    private BigDecimal payableAfterRebate;

}
