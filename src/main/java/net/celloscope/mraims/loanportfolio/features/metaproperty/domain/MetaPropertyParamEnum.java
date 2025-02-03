package net.celloscope.mraims.loanportfolio.features.metaproperty.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public enum MetaPropertyParamEnum {

    INSTALLMENT_PRECISION("InstallmentPrecision"),
    SERVICE_CHARGE_PRECISION("ServiceChargePrecision"),
    ROUNDING_TO("RoundingTo"),
    ROUNDING_LOGIC("RoundingLogic");

    @Getter
    private final String value;
}
