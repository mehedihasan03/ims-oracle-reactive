package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum QueryParams {
    OFFICE_ID("officeId"),
    SAMITY_ID("samityId"),
    SAMITY_DAY("samityDay"),
    LOAN_TERM("loanTerm"),
    FIELD_OFFICER_ID("fieldOfficerId"),
    MFI_ID("mfiId"),
    STATUS("status"),
    LOGIN_ID("loginId"),
    ACCOUNT_ID("accountId"),
    EMPLOYEE_ID("employeeId"),
    USER_ROLE("userRole"),
    ACCOUNT_NO("accountNo"),
    INSTITUTE_OID("instituteOid"),
    DISBURSEMENT_DATE("disbursementDate"),
    CUT_OFF_DATE("cutOffDate"),
    INSTALLMENT_NO("installmentNo"),
    NO_OF_PAST_INSTALLMENTS("noOfPastInstallments"),
    AMOUNT("amount"),
    INSTALLMENT_AMOUNT("installmentAmount"),
    DISBURSED_LOAN_AMOUNT("disbursedLoanAmount"),
    /*TRANSACTION_CODE_LOAN_REPAY("LOAN_REPAY"),
    TRANSACTION_CODE_SAVINGS_DEPOSIT("deposit"),
    TRANSACTION_CODE_SAVINGS_WITHDRAW("withdraw"),*/
    TRANSACTION_CODE("transactionCode"),
    TRANSACTION_DATE("transactionDate"),
    LOAN_ACCOUNT_ID("loanAccountId"),
    SAVINGS_ACCOUNT_ID("savingsAccountId"),
    CLOSING_DATE("closingDate"),
    TRANSACTION_ID("transactionId"),
    MEMBER_ID("memberId"),
    MANAGEMENT_PROCESS_ID("managementProcessId"),
    MONTHLY_REPAYMENT_FREQUENCY_DAY("monthlyRepaymentFrequencyDay"),
    SERVICE_CHARGE_CALCULATION_METHOD("serviceChargeCalculationMethod"),

    ACCRUED_INTEREST_AMOUNT("accruedInterestAmount"),
    CLOSING_TYPE("closingType"),

    COLLECTION_TYPE("collectionType"),
    FROM_DATE("fromDate"),
    TO_DATE("toDate"),
    SEARCH_TEXT("searchText"),
    LIMIT("limit"),
    OFFSET("offset"),
    START_DATE("startDate"),
    END_DATE("endDate"),

    INTEREST_POSTING_DATE("interestPostingDate"),
    FDR_ACTIVATION_DATE("fdrActivationDate"),

    INTEREST_RATE("interestRate"),
    INTEREST_RATE_FREQUENCY("interestRateFrequency"),
    INTEREST_RATE_PRECISION("interestRatePrecision"),
    ACCRUED_INTEREST_PRECISION("accruedInterestPrecision"),
    ROUNDING_MODE("roundingMode"),
    PAYMENT_PERIOD("paymentPeriod"),
    DAYS_IN_YEAR("daysInYear"),
    INTEREST_CALCULATION_DATE("interestCalculationDate"),
    INTEREST_CALCULATION_MONTH("interestCalculationMonth"),
    INTEREST_CALCULATION_YEAR("interestCalculationYear"),
    BALANCE_CALCULATION_METHOD("balanceCalculationMethod"),
    REMARKS("remarks"),
    SOURCE("source"),

    ENCASHMENT_DATE("encashmentDate"),
    EFFECTIVE_INTEREST_RATE("effectiveInterestRate"),
    PAYMENT_MODE("paymentMode"),
    REFERENCE_ACCOUNT_ID("referenceAccountId"),

    LOAN_PRODUCT_ID("loanProductId"),
    LOAN_AMOUNT("loanAmount"),
    NO_OF_INSTALLMENTS("noOfInstallments"),
    GRACE_DAYS("graceDays"),
    ROUNDING_TO_NEAREST_INTEGER("roundingToNearestInteger"),
    REPAYMENT_FREQUENCY("repaymentFrequency"),
    ROUNDING_LOGIC("roundingLogic"),
    SERVICE_CHARGE_RATE("serviceChargeRate"),
    ID("id"),
    LOAN_TERM_IN_MONTHS("loanTermInMonths");

    private final String value;

    QueryParams(String value) {
        this.value = value;
    }
}
