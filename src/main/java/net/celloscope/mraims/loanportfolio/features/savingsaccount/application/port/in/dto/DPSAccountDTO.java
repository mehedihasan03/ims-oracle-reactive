package net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto;

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
public class DPSAccountDTO {
    private String savingsAccountId;
    private String savingsAccountOid;
    private String savingsApplicationId;
    private String savingsProductId;
    private String savingsProdNameEn;
    private String savingsProdNameBn;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private Integer depositTerm;
    private String depositTermPeriod;
    private String depositEvery;
    private LocalDate acctStartDate;
    private BigDecimal savingsAmount;
    private BigDecimal balance;
    private BigDecimal interestRate;
    private String interestRateFrequency;
    private String interestCompoundingPeriod;
    private String interestPostingPeriod;
    private LocalDate acctEndDate;
    private BigDecimal maturityAmount;
    private String status;
    private String savingsTypeId;

    private String samityId;
    private String samityDay;
    private Integer monthlyRepayDay;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
