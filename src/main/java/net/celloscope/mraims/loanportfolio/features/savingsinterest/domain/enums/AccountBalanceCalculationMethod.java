package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.enums;

import lombok.Getter;

@Getter
public enum AccountBalanceCalculationMethod {
    ACCOUNT_BALANCE_CALCULATION_METHOD_DAILY_BASIS("daily_basis"),
    ACCOUNT_BALANCE_CALCULATION_METHOD_MONTHLY_OPEN_END_BASIS("monthly_open_end_basis"),

    ;
    private final String value;

    AccountBalanceCalculationMethod(String value) {
        this.value = value;
    }
}
