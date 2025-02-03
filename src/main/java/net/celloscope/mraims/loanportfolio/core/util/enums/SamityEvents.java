package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum SamityEvents {
	CANCELED("Canceled"),
	COLLECTED("Collected"),
	WITHDRAWN("Withdrawn"),
	LOAN_ADJUSTED("LoanAdjusted"),
	AUTHORIZED("Authorized"),
	TRANSACTION_COMPLETED("TransactionCompleted"),
	PASSBOOK_COMPLETED("PassbookCompleted"),
	COLLECTION_AUTHORIZED("CollectionAuthorized"),
	COLLECTION_TRANSACTION_COMPLETED("CollectionTransactionCompleted"),
	COLLECTION_PASSBOOK_COMPLETED("CollectionPassbookCompleted"),
	WITHDRAW_AUTHORIZED("WithdrawAuthorized"),
	WITHDRAW_TRANSACTION_COMPLETED("WithdrawTransactionCompleted"),
	WITHDRAW_PASSBOOK_COMPLETED("WithdrawPassbookCompleted"),
	AUTHORIZATION_COMPLETED("AuthorizationCompleted"),;
	
	private final String value;
	
	SamityEvents(String value) {
		this.value = value;
	}
}
