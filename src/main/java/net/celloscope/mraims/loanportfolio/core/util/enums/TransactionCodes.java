package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum TransactionCodes {

    LOAN_DISBURSEMENT("LOAN_DISBURSEMENT"),
    LOAN_REPAY("LOAN_REPAY"),
    LOAN_REBATE("LOAN_REBATE"),
    LOAN_REPAY_PRIN("LOAN_REPAY_PRIN"),
    LOAN_REPAY_SC("LOAN_REPAY_SC"),
    LOAN_ADJUSTMENT("LOAN_ADJUSTMENT"),
    SAVINGS_DEPOSIT("SAVINGS_DEPOSIT"),
    SAVINGS_WITHDRAW("SAVINGS_WITHDRAW"),
    SC_PROVISIONING("SC_PROVISIONING"),
    INTEREST_ACCRUED("INTEREST_ACCRUED"),
    INTEREST_POSTING("INTEREST_POSTING"),
    INTEREST_DEPOSIT("INTEREST_DEPOSIT"),
    WELFARE_FUND("WELFARE_FUND"),
    FEE_COLLECTION("FEE_COLLECTION"),
    LOAN_WRITE_OFF("LOAN_WRITE_OFF"),
    REVERSE_LOAN_REPAY("REVERSE_LOAN_REPAY"),
    REVERSE_SAVINGS_DEPOSIT("REVERSE_SAVINGS_DEPOSIT"),
    REVERSE_SAVINGS_WITHDRAW("REVERSE_SAVINGS_WITHDRAW"),
    ADJUSTMENT_SAVINGS_DEPOSIT("ADJUSTMENT_SAVINGS_DEPOSIT"),
    ADJUSTMENT_SAVINGS_WITHDRAW("ADJUSTMENT_SAVINGS_WITHDRAW"),
    ADJUSTMENT_LOAN_REPAY("ADJUSTMENT_LOAN_REPAY");

    private final String value;

    TransactionCodes(String value) {
        this.value = value;
    }
}
