package net.celloscope.mraims.loanportfolio.features.accounting.domain;

import lombok.Getter;

@Getter
public enum AisMetaDataEnum {
    // process name
    PROCESS_NAME_DISBURSEMENT("DISBURSEMENT"),
    PROCESS_NAME_SC_PROVISION("SC_PROVISION"),
    PROCESS_NAME_LOAN_COLLECTION("LOAN_COLLECTION"),
    PROCESS_NAME_LOAN_COLLECTION_NO_ADVANCE("LOAN_COLLECTION_NO_ADVANCE"),
    PROCESS_NAME_SAVINGS_COLLECTION("SAVINGS_COLLECTION"),
    PROCESS_NAME_WITHDRAW("WITHDRAW"),
    PROCESS_NAME_LOAN_ADJUSTMENT("LOAN_ADJUSTMENT"),
    PROCESS_NAME_INTEREST_ACCRUAL("INTEREST_ACCRUAL"),
    PROCESS_NAME_INTEREST_POSTING("INTEREST_POSTING"),
    PROCESS_NAME_WELFARE_FUND("WELFARE_FUND"),
    PROCESS_NAME_FEE_COLLECTION("FEE_COLLECTION"),
    PROCESS_NAME_REVERSE_LOAN_REPAY("REVERSE_LOAN_REPAY"),
    PROCESS_NAME_REVERSE_SAVINGS_DEPOSIT("REVERSE_SAVINGS_DEPOSIT"),
    PROCESS_NAME_REVERSE_SAVINGS_WITHDRAW("REVERSE_SAVINGS_WITHDRAW"),
    PROCESS_NAME_ADJUSTMENT_LOAN_REPAY("ADJUSTMENT_LOAN_REPAY"),
    PROCESS_NAME_ADJUSTMENT_SAVINGS_DEPOSIT("ADJUSTMENT_SAVINGS_DEPOSIT"),
    PROCESS_NAME_ADJUSTMENT_SAVINGS_WITHDRAW("ADJUSTMENT_SAVINGS_WITHDRAW"),

    // ledger key
    LEDGER_KEY_CASH_ON_HAND("CashOnHand"),
    LEDGER_KEY_CASH_AT_BANK("CashAtBank"),
    LEDGER_KEY_DEPOSIT_PAYABLE_SAVINGS_ACCOUNT("DepositPayableSavingsAccount"),
    LEDGER_KEY_LOAN_RECEIVABLE("LoanReceivable"),
    LEDGER_KEY_SERVICE_CHARGE_RECEIVABLE("ServiceChargeReceivable"),
    LEDGER_KEY_FEE_INCOME_LOAN_PROCESSING("FeeIncomeLoanProcessing"),
    LEDGER_KEY_SERVICE_CHARGE_INCOME("ServiceChargeIncome"),
    LEDGER_KEY_PRINCIPAL_OUTSTANDING("PrincipalOutstanding"),
    LEDGER_KEY_SERVICE_CHARGE_OUTSTANDING("ServiceChargeOutstanding"),
    LEDGER_KEY_PREPAYMENT_LOAN_INSTALLMENT("PrepaymentLoanInstallment"),
    LEDGER_KEY_PREPAYMENT_LOAN_INSTALLMENT_PRINCIPAL("PrepaymentLoanInstallmentPrincipal"),
    LEDGER_KEY_PREPAYMENT_LOAN_INSTALLMENT_SERVICE_CHARGE("PrepaymentLoanInstallmentServiceCharge"),
    LEDGER_KEY_COMPULSORY_SAVINGS("CompulsorySavings"),
    LEDGER_KEY_VOLUNTARY_SAVINGS("VoluntarySavings"),
    LEDGER_KEY_TERM_DEPOSIT("TermDeposit"),
    LEDGER_KEY_FIXED_DEPOSIT_PAYABLE ("FixedDepositPayable"),

    // ledger name
    LEDGER_NAME_CASH_ON_HAND("Cash On Hand"),
    LEDGER_NAME_DEPOSIT_PAYABLE_SAVINGS_ACCOUNT("Deposit Payable Savings Account"),
    LEDGER_NAME_LOAN_RECEIVABLE("Loan Receivable"),
    LEDGER_NAME_SERVICE_CHARGE_RECEIVABLE("Service Charge Receivable"),
    LEDGER_NAME_FEE_INCOME_LOAN_PROCESSING("Loan Processing Fee Income"),
    LEDGER_NAME_SERVICE_CHARGE_INCOME_LOAN("Loan Service Charge Income"),
    LEDGER_NAME_PREPAYMENT_LOAN_INSTALLMENT("Prepayment Loan Installment"),

    // Journal Entry Type

    JOURNAL_ENTRY_TYPE_DEBIT("Debit"),
    JOURNAL_ENTRY_TYPE_CREDIT("Credit"),

    // Journal Process

    JOURNAL_PROCESS_AUTO("Auto"),

    //
    YES("Yes"),
    NO("No")
    ;
    private final String value;

    AisMetaDataEnum(String value) {
        this.value = value;
    }

}
