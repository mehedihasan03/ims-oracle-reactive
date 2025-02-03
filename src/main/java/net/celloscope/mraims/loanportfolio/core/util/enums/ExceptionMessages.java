package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum
ExceptionMessages {
//	Process Management
//	Staging Data
	STAGING_DATA_IS_ALREADY_GENERATED_FOR_OFFICE("Staging Data is Already Generated for Office"),
	STAGING_DATA_GENERATION_PROCESS_IS_RUNNING_FOR_OFFICE("Staging Data Generation Process is Running For Office"),
	STAGING_DATA_IS_NOT_GENERATED_FOR_OFFICE("Staging Data is Not Generated for Office"),
	SAMITY_LIST_IS_NOT_VALID_FOR_STAGING_DATA_INVALIDATION("Samity List is Not Valid for Staging Data"),
	SAMITY_STAGING_DATA_GENERATION_IS_NOT_FINISHED("Samity Staging Data Generation is Not Finished"),
	SAMITY_STAGING_DATA_IS_ALREADY_DOWNLOADED("Samity Staging Data is Already Downloaded"),
	SAMITY_EVENT_FOUND("Samity Event Found"),

//	Day End Process
	DAY_END_PROCESS_IS_ALREADY_COMPLETED_FOR_OFFICE("Day End Process is Already Completed for Office"),
	DAY_END_PROCESS_IS_RUNNING_FOR_OFFICE("Day End Process is Running For Office"),
	DAY_END_PROCESS_IS_NOT_COMPLETED_FOR_OFFICE("Day End Process is Not Completed for Office"),
	
//	 DISBURSEMENT
	LOAN_DISBURSEMENT_FAILED("Loan Disbursement Failed"),
	LOAN_REPAYMENT_SCHEDULE_ALREADY_GENERATED("Loan Repayment Schedule Is Already Generated!"),
	LOAN_ACCOUNT_STATUS_NOT_APPROVED("Loan Account Status Is Not 'Approved'!"),
	
//	 COLLECTION STAGING DATA
	NO_COLLECTION_STAGING_DATA_FOUND_FOR_OFFICE("No Collection Staging Data Found for Office."),
	NO_STAGING_DATA_FOUND("No Staging Data Found."),
	NO_STAGING_DATA_FOUND_FOR_SAMITY("No Staging Data Found For Samity : "),
	NO_STAGING_DATA_FOUND_WITH_ACCOUNT_ID("No Staging Data Found With Account ID : "),
	NO_STAGING_DATA_FOUND_WITH_FIELD_OFFICER_ID("No Staging Data Found With Field Officer ID : "),
	NO_STAGING_DATA_FOUND_WITH_MEMBER_ID("No Staging Account Data Found With Member ID : "),
	
//	 INTERNAL SERVER ERROR
	SOMETHING_WENT_WRONG("Something Went Wrong. Please Try Again Later."),
	
//	 AUTHORIZE COLLECTION
	NO_COLLECTION_DATA_FOUND("No Collection Data Found"),
	
//	 PAYMENT COLLECTION
	COLLECTION_DATA_MISMATCH_FOR_SAMITY("Collection Data Mismatch for Samity."),
	COLLECTION_DATA_MISMATCH_FOR_STAGING_DATA_ID_AND_ACCOUNT_ID("Collection Data Mismatch for Staging Data ID And Account ID."),
	COLLECTION_DATA_MISMATCH_FOR_COLLECTION_TYPE("Collection Data Mismatch for Collection Type."),
	
//	 PASSBOOK
	
	VALIDATION_FAILED("Validation Failed!"),
	VALIDATION_FAILED_WHILE_WITHDRAWING("Validation Failed while Withdrawing!"),
	WITHDRAW_NOT_ALLOWED_NO_DEPOSIT_RECORD("No Deposit Record in Savings Account. Withdraw not allowed."),
	WITHDRAW_DATA_NOT_FOUND("Withdraw Data Not Found."),
	SAVINGS_ACCOUNT_STATUS_IS_NOT_ACTIVE("Savings Account Status Is Not 'Active'."),
	SAVINGS_ACCOUNT_STATUS_INVALID("Savings Account Status 'Invalid'!"),
	PASSBOOK_ENTRY_EXISTS_YET_SAVINGS_ACCOUNT_STATUS_NOT_ACTIVE("Passbook Entry Exists But Savings Account Status 'Inactive'"),
	
//	 WITHDRAW
	NO_WITHDRAW_DATA_FOUND("No Withdraw Data Found!"),
	INSUFFICIENT_BALANCE_FOR_WITHDRAW_IN_SAVINGS_ACCOUNT("Insufficient Balance for Withdraw In Savings Account : "),
	SAVINGS_ACCOUNT_IS_NOT_ACTIVE("Savings Account is Not Active : "),
	
	NO_SAMITY_FOUND_FOR_OFFICER_LIST("No Samity Found For Officer List"),
	NO_WITHDRAW_STAGING_DATA_FOUND_FOR_OFFICE("No Withdraw Staging Data Found For Office"),

	SAMITY_CANNOT_BE_CANCELED("Regular Samity cannot be Canceled."),
	SAMITY_CANCELED("Samity Canceled Successfully : "),

//	    TRANSACTION
	FAILED_TO_SAVE_TRANSACTION("Failed to save Transaction"),

//	 ACCRUED INTEREST

	INTEREST_ALREADY_ACCRUED("Interest already accrued for this month."),
	INTEREST_CANNOT_BE_CALCULATED("Interest Cannot be calculated for month before A/C opening Month."),

//	 FDR

	ACCOUNT_ALREADY_ACTIVATED("Account Already Activated"),
	MINIMUM_AMOUNT_REQUIREMENT_NOT_MET("Minimum Amount Requirement Not Met."),
	AMOUNT_EXCEEDED_MAXIMUM_AMOUNT("FDR Amount Exceeded Maximum Amount."),
	FDR_AMOUNT_MISMATCH("FDR Amount Mismatch!"),
	ACCOUNT_NOT_ELIGIBLE_FOR_ACTIVATION("Account Not Eligible For Activation!"),
	SAVINGS_ACCOUNT_NOT_FOUND("Savings Account Not Found."),
	SCHEDULE_ALREADY_EXISTS_FOR("Schedule Already exists for : "),
	EMPTY_LOGINID("Login Id cannot be empty"),
	EMPTY_MFIID("MFI Id cannot be empty"),
    NO_REBATE_DATA_FOUND_FOR_OFFICE("No Rebate Data Found For Office"),


	NO_REPAYMENT_SCHEDULE_HISTORY_FOUND("No Repayment Schedule History Found"),
	NO_REPAYMENT_SCHEDULE_EDIT_HISTORY_FOUND("No Repayment Schedule Edit History Found"),
	META_PROPERTY_NOT_FOUND("Meta Property Not Found");

	private final String value;

	ExceptionMessages(String value) {
		this.value = value;
	}
}
