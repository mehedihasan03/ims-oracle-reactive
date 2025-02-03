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
public class RepaymentScheduleCommand {
    private BigDecimal loanAmount;
    private BigDecimal totalServiceCharge;
    private BigDecimal accumulatedLoanAmount;
    private BigDecimal totalOutstandingAmount;
    private BigDecimal outstandingPrincipal;
    private BigDecimal outstandingServiceCharge;
    private BigDecimal installmentAmount;
    private BigDecimal installmentPrincipal;
    private BigDecimal installmentServiceCharge;
    private Integer noOfInstallments;
    private String repaymentFrequency;
    private Integer graceDays;
    private LocalDate cutOffDate;

    private String officeId;
    private Integer monthlyRepaymentFrequencyDay;
    private BigDecimal overdueAmount;
    private BigDecimal annualServiceChargeRate;
    private LocalDate disbursementDate;
    private Integer loanTermInMonths;
    // member information

    private String loanAccountId;
    private String memberId;
    private String samityId;
    private String mfiId;
    private String loginId;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
