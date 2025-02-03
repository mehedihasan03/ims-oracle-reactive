package net.celloscope.mraims.loanportfolio.features.dps.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DPSClosure {
    private String savingsAccountId;
    private String savingsApplicationId;
    private String savingsProductId;
    private String savingsProdNameEn;
    private String memberId;
    private String officeId;
    private String memberNameEn;
    private String memberNameBn;
    private LocalDate acctStartDate;
    private LocalDate acctEndDate;
    private LocalDate acctCloseDate;
    private BigDecimal savingsAmount;
    private BigDecimal actualInterestRate;
    private BigDecimal effectiveInterestRate;
    private String interestRateFrequency;
    private String interestPostingPeriod;
    private String interestCompoundingPeriod;
    private BigDecimal totalInterest;
    private BigDecimal closingInterest;
    private BigDecimal closingAmount;
    private String paymentMode;
    private String referenceAccountId;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private LocalDateTime rejectedOn;
    private String rejectedBy;
    private String status;
    private String remarks;

    private String managementProcessId;
    private String savingsTypeId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
