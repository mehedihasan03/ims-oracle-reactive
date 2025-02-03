package net.celloscope.mraims.loanportfolio.features.migrationV3;

import lombok.Getter;

@Getter
public enum MigrationEnums {

    MIGRATION("MIGRATION"),
    PERSON_SHORT_NAME("PER"),
    MEMBER_SHORT_NAME("MEM"),
    MEM_SMT_OFF_PRI_MAP_SHORT_NAME("MSOPM"),
    LOAN_PRODUCT_SHORT_NAME("LP"),
    SAVINGS_PRODUCT_SHORT_NAME("SP"),
    SAVINGS_ACCOUNT_SHORT_NAME("SACC"),
    SERVICE_CHARGE_CHART_SHORT_NAME("SC"),
    INTEREST_CHART_SHORT_NAME("IC"),
    LOAN_ACCOUNT_APPLICATION_SHORT_NAME("LAP"),
    LOAN_ACCOUNT_SHORT_NAME("LA"),
    STATUS_ACTIVE("Active"),
    STATUS_APPROVED("Approved"),
    SAVINGS_INTEREST_RATE_TERM("Fixed"),
    SAVINGS_INTEREST_RATE_FREQUENCY("Yearly"),
    SAVINGS_INTEREST_CALCULATE_USING("End of Day Balance"),
    SAVINGS_INTEREST_POSTING_PERIOD("Yearly"),
    SAVINGS_INTEREST_COMPOUNDING_PERIOD("Yearly"),
    SAVINGS_ACCOUNT_PROPOSAL_SHORT_NAME("SAA"),
    SAVINGS_DEPOSIT_EVERY("Monthly"),
    SAVINGS_DEPOSIT_PERIOD("Year"),

    ;

    private final String value;
    MigrationEnums(String value) {
        this.value = value;
    }
}
