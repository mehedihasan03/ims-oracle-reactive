package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanRepaymentScheduleRequestDTO {
    private BigDecimal loanAmount;
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFrequency;
    private Integer noOfInstallments;
    private BigDecimal installmentAmount;
    private Integer graceDays;
    private LocalDate disburseDate;
    private String samityDay;
    private Integer loanTerm;
    private String repaymentFrequency;
    private String roundingLogic;
    private String loanAccountId;
    private String memberId;
    private String mfiId;
    private String status;
    private String loginId;

    private Integer monthlyRepaymentFrequencyDay;
    private String serviceChargeCalculationMethod;
    private String roundingToNearestIntegerLogic;
    private Integer roundingToNearest;

    private Integer serviceChargeAmountPrecision;
    private Integer serviceChargeRatePrecision;
    private Integer installmentPrecision;

    private String officeId;
    private BigDecimal serviceChargeRatePerPeriod;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
