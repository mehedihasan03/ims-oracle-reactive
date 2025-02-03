package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum MetaPropertyEnum {
    INSTALLMENT_PRECISION("InstallmentPrecision"),
    SERVICE_CHARGE_PRECISION("ServiceChargePrecision"),
    ROUNDING_TO("RoundingTo"),
    ROUNDING_LOGIC("RoundingLogic"),


    // Rounding Mode
    ROUNDING_MODE_HALF_UP("HALFUP"),
    ROUNDING_MODE_HALF_DOWN("HALFDOWN"),
    ROUNDING_MODE_UP("UP"),
    ROUNDING_MODE_DOWN("DOWN"),

    ;

    private final String value;

    MetaPropertyEnum(String value) {
        this.value = value;
    }
}
