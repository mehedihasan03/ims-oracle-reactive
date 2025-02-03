package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingsAccountProductEntity {
    private String savingsAccountId;
    private String savingsAccountOid;
    private LocalDate acctStartDate;
    private String interestRateTerms;
    private String interestRateFrequency;
    private String interestCalculatedUsing;
    private String interestPostingPeriod;
    private String interestCompoundingPeriod;
    private String balanceRequiredInterestCalc;
    private String status;
    private String memberId;
    private String mfiId;

    private String savingsProductId;
    private String savingsTypeId;

    private BigDecimal interestRate;
    private BigDecimal provisionInterestRate;
    private String interestPostingDates;

}
