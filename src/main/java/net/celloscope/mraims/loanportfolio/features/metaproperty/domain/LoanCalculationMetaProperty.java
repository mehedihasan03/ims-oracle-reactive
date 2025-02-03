package net.celloscope.mraims.loanportfolio.features.metaproperty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.RoundingMode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanCalculationMetaProperty {
    private Integer serviceChargeRatePrecision;
    private Integer serviceChargeAmountPrecision;
    private String roundingLogic;
    private String daysInYear;
    private String serviceChargeDeductionMethod;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
