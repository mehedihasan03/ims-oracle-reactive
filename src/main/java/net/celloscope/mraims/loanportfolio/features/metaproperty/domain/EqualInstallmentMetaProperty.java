package net.celloscope.mraims.loanportfolio.features.metaproperty.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EqualInstallmentMetaProperty {
    private Integer installmentPrecision;
    private Integer serviceChargeRatePrecision;
    private Integer serviceChargePrecision;
    private Integer roundingToNearestInteger;
    private String roundingLogic;
    private Double maxDeviationPercentage;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }

}
