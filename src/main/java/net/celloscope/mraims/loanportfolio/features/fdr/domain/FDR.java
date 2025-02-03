package net.celloscope.mraims.loanportfolio.features.fdr.domain;

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
public class FDR {
    private String savingsAccountId;
    private String savingsApplicationId;
    private String savingsProductId;
    private String savingsProdNameEn;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private LocalDate acctStartDate;
    private BigDecimal savingsAmount;
    private BigDecimal interestRate;
    private String interestRateFrequency;
    private String interestPostingPeriod;
    private LocalDate acctEndDate;
    private BigDecimal maturityAmount;
    private String status;

    private LocalDate acctCloseDate;
    private BigDecimal closingAmount;
    private BigDecimal totalInterest;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
