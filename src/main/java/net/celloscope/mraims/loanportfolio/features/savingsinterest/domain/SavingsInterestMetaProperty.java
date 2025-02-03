package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingsInterestMetaProperty {
    private Integer accruedInterestPrecision;
    private Integer interestRatePrecision;
    private String roundingLogic;
    private String daysInYear;
    private String accountBalanceCalculationMethod;
}
