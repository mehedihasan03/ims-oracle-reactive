package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepaymentScheduleViewDTO {

    private Integer installNo;
    private BigDecimal beginPrinBalance;
    private BigDecimal scheduledPayment;
    private BigDecimal extraPayment;
    private BigDecimal totalPayment;
    private BigDecimal principal;
    private BigDecimal serviceCharge;
    private BigDecimal endPrinBalance;

    private LocalDate installDate;
    private String loanRepayScheduleId;
    private BigDecimal serviceChargeRatePerPeriod;
    private BigDecimal annualServiceChargeRate;
}
