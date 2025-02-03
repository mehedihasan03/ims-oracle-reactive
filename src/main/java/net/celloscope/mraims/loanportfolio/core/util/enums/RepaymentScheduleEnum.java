package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum RepaymentScheduleEnum {
    FREQUENCY_YEARLY("Yearly"),
    FREQUENCY_MONTHLY("Monthly"),
    FREQUENCY_WEEKLY("Weekly"),

    NATURAL_ROUNDING("HalfUp"),
    ROUNDING_UP("Up"),
    ROUNDING_DOWN("Down"),
    NO_ROUNDING("No_Rounding"),

    NO_ROUNDING_TO_INTEGER("No_Rounding_To_Integer"),

    SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE("Declining_Balance"),
    SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE_EQUAL_INSTALLMENT("Declining-Balance-with-Equal-Installments"),
    SERVICE_CHARGE_CALCULATION_METHOD_FLAT("Flat"),


    ;
    private final String value;

    RepaymentScheduleEnum(String value) {
        this.value = value;
    }
}
