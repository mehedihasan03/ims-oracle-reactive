package net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculationMetaProperty {
    private Integer accruedInterestPrecision;
    private Integer interestRatePrecision;
    private Integer equalInstallmentRoundingToNext;
    private Integer installmentPrecision;
    private Integer serviceChargePrecision;
    private String roundingLogic;
    private String daysInYear;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
