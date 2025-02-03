package net.celloscope.mraims.loanportfolio.features.fdr.domain;

import lombok.Getter;

@Getter
public enum FDREnum {

    INTEREST_POSTING_PERIOD_MONTHLY("Monthly"),
    INTEREST_POSTING_PERIOD_QUARTERLY("Quarterly"),
    INTEREST_POSTING_PERIOD_YEARLY("Yearly"),
    INTEREST_POSTING_PERIOD_HALF_YEARLY("Half-Yearly"),

    DEPOSIT_TERM_PERIOD_MONTH("Month"),
    DEPOSIT_TERM_PERIOD_YEAR("Year"),

    INTEREST_RATE_FREQUENCY_MONTHLY("Monthly"),
    INTEREST_RATE_FREQUENCY_YEARLY("Yearly"),

    ;


    private final String value;

    FDREnum(String value) {
        this.value = value;
    }
}
