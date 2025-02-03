package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculateInterestData {

    private String savingsAccountId;
    private BigDecimal interestRate;
    private BigDecimal provisionInterestRate;
    private String interestRateFrequency;
    private Integer interestRatePrecision;
    private Integer accruedInterestPrecision;
    private RoundingMode roundingMode;
    private String daysInYear;
    private LocalDate interestCalculationDate;
    private Integer interestCalculationMonth;
    private Integer interestCalculationYear;
    private String balanceCalculationMethod;
    private BigDecimal balanceRequiredInterestCalc;
    private String accountBalanceCalculationMethod;

    private String interestPostingPeriod;
    private String interestCompoundingPeriod;
    private String interestRateTerms;
    private LocalDate acctStartDate;

    private String savingsProductId;
    private String savingsTypeId;

    private String mfiId;
    private String memberId;
    private String savingsAccountOid;

    private String managementProcessId;
    private String processId;
    private String officeId;
    private String samityId;
    private String loginId;
    private LocalDate businessDate;
}
