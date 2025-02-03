package net.celloscope.mraims.loanportfolio.repaymentSchedule.application.service.helpers.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/*@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor*/
public class RepaymentScheduleTestParamDTO {
    BigDecimal principal;
    BigDecimal serviceChargeRate;
    String serviceChargeRateFrequency;                   // Yearly, Monthly
    Integer noOfInstallments;
    String paymentPeriod;                                    // null -> to calculate SC on Tenure (pDef = No)
    String daysInYear;                                       // null -> to calculate SC on Tenure (pDef = No)
    BigDecimal installmentAmount;
    Integer graceDays;
    LocalDate disburseDate;
    String samityDay;
    String loanTerm;
    String roundingLogic;
}
