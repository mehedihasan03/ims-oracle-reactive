package net.celloscope.mraims.loanportfolio.core.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MetaProperty {
    private BigDecimal installmentPrecision;
    private BigDecimal serviceChargePrecision;
    private BigDecimal serviceChargeRatePrecision;
    private Integer roundingTo;
    private RoundingMode roundingLogic;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
