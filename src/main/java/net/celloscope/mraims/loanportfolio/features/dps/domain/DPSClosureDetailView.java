package net.celloscope.mraims.loanportfolio.features.dps.domain;

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
public class DPSClosureDetailView {
    private String savingsAccountId;
    private String savingsApplicationId;
    private String savingsProductId;
    private String savingsProdNameEn;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;

    private LocalDate acctStartDate;
    private LocalDate acctEndDate;
    private LocalDate acctCloseDate;
    private BigDecimal savingsAmount;
    private BigDecimal maturityAmount;
    private BigDecimal closingAmount;
    private BigDecimal totalInterest;

    private BigDecimal interestRate;
    private BigDecimal effectiveInterestRate;
    private String interestRateTerms;
    private String interestRateFrequency;
    private String interestPostingPeriod;
    private String interestCompoundingPeriod;
    private String status;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
