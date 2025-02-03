package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MigrationRepaymentScheduleCommand {
    private String officeId;
    private String loanAccountId;
    private String memberId;
    private String mfiId;
    private String status;
    private String loginId;
    private BigDecimal loanAmount;
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFrequency;
    private Integer noOfInstallments;
    private String repaymentFrequency;
    private Integer graceDays;
    private LocalDate disburseDate;
    private String samityDay;
    private String roundingMode;
    private String roundingInstallmentToNearestIntegerLogic;
    private Integer roundingInstallmentToNearestInteger;
    private Boolean isMonthly;
    private Integer monthlyRepaymentFrequencyDay;
    private Integer serviceChargeRatePrecision;
    private Integer principalAmountPrecision;
    private Integer installmentAmountPrecision;

    private LocalDate cutOffDate;
    private Integer noOfPastInstallments;

    private BigDecimal installmentAmount;
    private BigDecimal disbursedLoanAmount;

    private Integer loanTermInMonths;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
