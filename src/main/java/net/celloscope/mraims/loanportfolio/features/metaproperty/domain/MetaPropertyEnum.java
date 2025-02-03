package net.celloscope.mraims.loanportfolio.features.metaproperty.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum MetaPropertyEnum {

    EQUAL_INSTALLMENT("Equal Installment Meta-Property"),
    SAVINGS_INTEREST("Savings Interest Meta-Property"),
    ACCOUNTING("Accounting Meta-Property"),
    LOAN_REPAY_SCHEDULE("Loan Repay Schedule Meta-Property"),
    LOAN_CALCULATION("Loan Calculation Meta-Property"),

    SERVICE_CHARGE_DEDUCTION_METHOD_SCHEDULE_BASED("schedule_based"),
    SERVICE_CHARGE_DEDUCTION_METHOD_RATE_BASED("rate_based"),

    EQUAL_INSTALLMENT_META_PROPERTY_ID("101"),
    SAVINGS_INTEREST_META_PROPERTY_ID("102"),
    LOAN_REPAY_SCHEDULE_META_PROPERTY_ID("103"),
    SMS_NOTIFICATION_META_PROPERTY_ID("104"),
    LOAN_TERM_META_PROPERTY_ID("105"),
    ACCOUNTING_META_PROPERTY_ID("106"),
    LOAN_CALCULATION_META_PROPERTY_ID("107"),

    ;

    private final String value;
}
