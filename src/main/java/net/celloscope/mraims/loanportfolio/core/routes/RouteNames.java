package net.celloscope.mraims.loanportfolio.core.routes;

public class RouteNames {

	public static String MRA_API_BASE_URL = "/mra-ims/mfi/process/api/v1";

	public static String MRA_API_BASE_URL_V2 = "/mra-ims/mfi/process/api/v2";
	public static String MRA_API_BASE_URL_V3 = "/mra-ims/mfi/process/api/v3";

	// Staging Data
	public static final String STAGING_DATA_BASE_URL = "/staging-data";
	public static final String POST_GENERATE_STAGING_DATA = "/generate";
	public static final String GET_STAGING_DATA_STATUS = "/status";
	public static final String GET_STAGING_DATA_DETAIL_VIEW_BY_SAMITY_ID = "";
	public static final String GET_STAGING_DATA_DETAIL_VIEW_BY_ACCOUNT_ID = "/by-account-id";
	public static final String GET_STAGING_DATA_DETAIL_VIEW_BY_MEMBER_ID = "/by-member-id";
	public static final String GET_STAGING_DATA_GRID_VIEW = "";
	public static final String POST_EXCEPTION_STAGING_DATA_BY_SAMITY = "/exception";
	public static final String POST_REGENERATE_STAGING_DATA_BY_SAMITY = "/regenerate";


	// welfare fund
	public static final String GET_WELFARE_FUND_DATA = "/welfare-fund";
	public static final String COLLECT = "/collect";
	public static final String RESET_WELFARE_FUND_COLLECTION = "/reset-welfare-fund-collection";

	// write off collection
	public static final String WRITE_OFF_COLLECTION = "/write-off-collection";
	public static final String LOAN_ACCOUNT_DETAILS_BY_ID = "/loan-account-details-by-id";

	// transaction
	public static final String TRANSACTION = "/transaction";
	public static final String PASSBOOK = "/passbook";
	public static final String SPECIAL_COLLECTION = "/special-collection";
	public static final String WITHDRAW_COLLECTION = "/withdraw-collection";

	public static final String GRID_VIEW = "/grid-view";
	public static final String DETAIL_VIEW = "/detail-view";
	public static final String HISTORY = "/history";
	public static final String DETAILS = "/details";
	public static final String REPORT_BY_SAMITY = "/report-by-samity";
	public static final String CREATE_TRANSACTION = "/create";
	public static final String CREATE_TRANSACTION_FOR_ACCRUED_INTEREST = "/create-accrued-interest";
	public static final String CREATE_TRANSACTION_FOR_HALF_YEARLY_ACCRUED_INTEREST = "/create-transaction-accrued-interest-posting";
	public static final String DISBURSE = "/disburse";
	public static final String DISBURSE_MIGRATION = "/disburse/migration";
	public static String GET_REPAYMENT_SCHEDULE_DECLINING_BALANCE_EQUAL_INSTALLMENT = "/repayment-schedule-declining-ei";
	public static String VIEW_REPAYMENT_SCHEDULE_DECLINING_BALANCE_EQUAL_INSTALLMENT = "/view-repayment-schedule-declining-ei";
	public static String VIEW_REPAYMENT_SCHEDULE_FLAT = "/view-repayment-schedule-flat";
	public static String VIEW_REPAYMENT_SCHEDULE_FLAT_MIGRATION = "/view-repayment-schedule-flat/migration";
	public static String GET_REPAYMENT_SCHEDULE_DECLINING_BALANCE = "/repayment-schedule-declining";
	public static String PRINT_REPAYMENT_SCHEDULE_WITH_DATES = "/print-repayment-schedule-with-dates";
	public static String GET_REPAYMENT_INFO = "/get-repayment-info";
	public static String GET_REPAYMENT_SCHEDULE_BY_LOAN_ACCOUNT_ID = "/get-repayment-schedule";
	public static String GET_EQUAL_INSTALLMENT = "/get-equal-installment-amount";
	public static String GET_EQUAL_INSTALLMENT_V2 = "/get-equal-installment-amount-v2";
	public static String CREATE_PASSBOOK_ENTRY = "/create-passbook-entry";
	public static String GENERATE_DPS_REPAYMENT_SCHEDULE = "/dps-repayment-schedule/generate";
	public static String GENERATE_REPAYMENT_SCHEDULE_FLAT_INSTALLMENT_INFO = "/repayment-schedule-flat-installment/generate";

	public static final String DETAIL_VIEW_COLLECTION_STAGING_DATA_BY_SAMITY = "/collection-staging-data/get-by-samity-id";
	public static final String DETAIL_VIEW_COLLECTION_STAGING_DATA_BY_ACCOUNT = "/collection-staging-data/get-by-account-id";
	public static final String DETAIL_VIEW_COLLECTION_STAGING_DATA_BY_MEMBER = "/collection-staging-data/get-by-member-id";
	public static String PAYMENT_COLLECTION = "/collection-staging-data/collect";
	public static String PAYMENT_COLLECTION_BY_FIELD_OFFICER = "/collection-staging-data/collect-by-field-officer-id";
	public static String AUTHORIZE_COLLECTION = "/collection-staging-data/authorize-by-samity-id";
	public static String UNAUTHORIZE_COLLECTION = "/collection-staging-data/unauthorize-by-samity-id";
	public static String UPDATE_PAYMENT_COLLECTION = "/collection-staging-data/update-by-samity-id";
	public static String LOCK_COLLECTION = "/collection-staging-data/lock-by-samity-id";
	public static String UNLOCK_COLLECTION = "/collection-staging-data/unlock-by-samity-id";
	public static String REJECT_COLLECTION = "/collection-staging-data/reject-by-samity-id";
	public static String COMMIT_COLLECTION = "/collection-staging-data/commit-by-samity-id";
	public static String MRA_BASE_URL = "/api";
	public static String MRA_API_VERSION = "/v1";
	public static String GRID_VIEW_DATA_STAGING_COLLECTION_BY_OFFICE = "/collection-staging-data-by-office-id";
	public static String GRID_VIEW_DATA_STAGING_COLLECTION = "/collection-staging-data";
	public static String GRID_VIEW_DATA_STAGING_SPECIAL_COLLECTION_BY_OFFICE = "/special-collection-staging-data-by-office-id";
	public static String GRID_VIEW_DATA_STAGING_SPECIAL_COLLECTION = "/special-collection-staging-data";
	public static String GRID_VIEW_AUTHORIZATION = "/collection-staging-data-for-authorization";
	public static String GRID_VIEW_SPECIAL_COLLECTION_AUTHORIZATION = "/special-collection-staging-data-for-authorization";
	public static String CREATE_PASSBOOK_ENTRY_LOAN = "/create-passbook-entry-loan";
	public static String CREATE_PASSBOOK_ENTRY_SAVINGS = "/create-passbook-entry-savings";
	public static String CREATE_PASSBOOK_ENTRY_SAVINGS_WITHDRAW = "/create-passbook-entry-savings-withdraw";
	public static String CREATE_PASSBOOK_ENTRY_ACCRUED_INTEREST_DEPOSIT = "/create-passbook-entry-interest-deposit";

	public static String DETAIL_VIEW_COLLECTION_OF_FIELD_OFFICER = "/collection-staging-data/get-by-field-officer-id";
	// withdraw staging data
	public static final String WITHDRAW_STAGING_DATA_BASE_URL = "/withdraw-staging-data";
	public static final String WITHDRAW = "/withdraw";
	public static final String EDIT = "/edit";
	public static final String GRID_VIEW_WITHDRAW_STAGING_DATA_BY_OFFICE = "/withdraw-staging-data-by-office-id";
	public static final String GRID_VIEW_WITHDRAW_STAGING_DATA = "/withdraw-staging-data";
	public static final String GRID_VIEW_WITHDRAW_STAGING_DATA_AUTHORIZATION = "/withdraw-staging-data-for-authorization";
	public static final String DETAIL_VIEW_WITHDRAW_STAGING_DATA_BY_SAMITY_ID = "/withdraw-staging-data/get-by-samity-id";
	public static final String DETAIL_VIEW_WITHDRAW_STAGING_DATA_BY_MEMBER_ID = "/withdraw-staging-data/get-by-member-id";
	public static final String DETAIL_VIEW_WITHDRAW_STAGING_DATA_BY_ACCOUNT_ID = "/withdraw-staging-data/get-by-account-id";
	public static String STAGE_WITHDRAW = "/withdraw-staging-data/withdraw";
	public static String AUTHORIZE_WITHDRAW = "/withdraw-staging-data/authorize-by-samityId";
	public static String LOAN_REBATE = "/loan-rebate";

	public static String SAVINGS_ACCRUED_INTEREST_BY_DATE = "/savings-accrued-interest-by-date";
	public static String SAVINGS_ACCRUED_INTEREST_BY_MONTH = "/savings-accrued-interest-by-month";
	public static String CALCULATE_ACCRUED_INTEREST_BY_MONTH = "/calculate-savings-accrued-interest-by-month";
	public static String SAVINGS_ACCRUED_INTERESTS_BY_MANAGEMENT_PROCESS_ID_AND_OFFICE_ID = "/accrued-interests/";
	public static String CANCEL_SAMITY = "/cancel-samity";
	public static String RESTORE_CANCELLED_SAMITY = "/restore-canceled-samity";

	// Process Management
	public static final String PROCESS_MANAGEMENT_BASE_URL = "/process-management";
	public static final String PROCESS_MANAGEMENT_RUN_DAY_END_PROCESS = "/run-day-end-process";
	public static final String PROCESS_MANAGEMENT_RUN_MONTH_END_PROCESS = "/run-month-end-process";
	public static final String PROCESS_MANAGEMENT_RUN_FORWARD_DAY_ROUTINE = "/run-forward-day-routine";
	public static final String PROCESS_MANAGEMENT_GRID_VIEW_PROCESS_DASHBOARD_BY_MFI = "/process-dashboard/get-by-mfi";
	public static final String PROCESS_MANAGEMENT_GRID_VIEW_PROCESS_DASHBOARD_BY_OFFICE = "/process-dashboard/get-by-office";

	public static final String GET_PROCESS_MANAGEMENT_FOR_OFFICE = "/get-by-office-id";
	public static String CALCULATE_AND_SAVE_ACCRUED_INTEREST_BY_MONTH = "/save-accrued-interest-by-month";
	public static String GET_ACCRUED_INTEREST_ENTITIES = "/get-accrued-interest-entities";
	public static String GET_DPS_MATURITY_AMOUNT = "/dps/maturity-amount";
	public static String POST_SAVINGS_INTEREST = "/post-savings-interest";

	// Calendar API
	public static final String GET_NEXT_BUSINESS_DAY_FROM_CALENDAR = "/get-next-business-day";

	public static final String RESCHEDULE_REPAY_SCHEDULE_ON_SAMITY_CANCEL = "/loan-repay-schedule/reschedule";

	public static String FDR_INTEREST_POSTING = "/post-fdr-interest";
	public static String GET_SCHEDULE = "/get-fdr-schedule";
	public static String ACTIVATE_FDR_ACCOUNT = "/activate-fdr";
	public static String FDR_GRID_VIEW = "/savings-account/fdr/by-office/grid-view";
	public static String FDR_DETAIL_VIEW = "/savings-account/fdr/by-account/detail-view";
	public static String FDR_CLOSURE = "/savings-account/fdr/closure";
	public static String FDR_CLOSURE_GRID_VIEW = "/savings-account/fdr-closure/by-office/grid-view";
	public static String FDR_CLOSURE_DETAIL_VIEW = "/savings-account/fdr-closure/by-account/detail-view";
	public static String FDR_AUTHORIZATION = "/savings-account/fdr/closure-authorize";
	public static String FDR_REJECTION = "/savings-account/fdr/closure-reject";
	public static String FDR_CLOSING_INFO = "/savings-account/fdr-closure/by-account/info";

	public static String DPS_GRID_VIEW = "/savings-account/dps/by-office/grid-view";
	public static String DPS_CLOSURE_GRID_VIEW = "/savings-account/dps-closure/by-office/grid-view";
	public static String DPS_DETAIL_VIEW = "/savings-account/dps/by-account/detail-view";
	public static String DPS_CLOSURE_DETAIL_VIEW = "/savings-account/dps-closure/by-account/detail-view";
	public static String DPS_CLOSING_INFO = "/savings-account/dps-closure/by-account/info";
	public static String DPS_CLOSURE = "/savings-account/dps/closure";
	public static String DPS_AUTHORIZATION = "/savings-account/dps/closure-authorize";
	public static String DPS_REJECTION = "/savings-account/dps/closure-reject";

	public static String SAVINGS_ACCOUNT = "/savings-account";
	public static String CREATE_SAVINGS_CLOSURE = "/savings-closure";
	public static String REJECT_SAVINGS_CLOSURE = "/reject-savings-closure";
	public static String AUTHORIZE_SAVINGS_CLOSURE = "/authorize-savings-closure";

	// Process Management v2
	public final static String STAGING_COLLECTION_DATA_BASE_URL = "/staging-collection-data";
	public final static String STAGING_WITHDRAW_DATA_BASE_URL = "/staging-withdraw-data";
	public final static String AUTHORIZATION = "/authorization";
	public final static String DAY_END_PROCESS = "/day-end-process";
	public final static String DAY_END_PROCESS_REVERT_AIS = "/revert-ais";
	public final static String DAY_END_PROCESS_REVERT_MIS = "/revert-mis";
	public final static String MONTH_END_PROCESS = "/month-end-process";
	public final static String MONTH_END_PROCESS_REVERT = "/revert";
	public final static String LOAN_ADJUSTMENT = "/loan-adjustment";

	// Router Accessibility
	public static final String BY_OFFICE = "/by-office";
	public static final String BY_FIELD_OFFICER = "/by-field-officer";
	public static final String BY_SAMITY = "/by-samity";
	public static final String BY_MEMBER = "/by-member";
	public static final String BY_ACCOUNT = "/by-account";
	public static final String BY_ENTITY = "/by-entity";

	public final static String GET_LIST = "/list";
	public final static String GET_GRID_VIEW = "/grid-view";
	public final static String GET_TAB_VIEW = "/tab-view";
	public final static String GET_DETAIL_VIEW = "/detail-view";
	public final static String REGULAR_SAMITY = "/regular-samity";
	public final static String SPECIAL_SAMITY = "/special-samity";
	public final static String PAYMENT = "/payment";
	public final static String SUBMIT = "/submit";
	public final static String LOCK = "/lock";
	public final static String UNLOCK = "/unlock";
	public final static String DELETE = "/delete";
	public final static String SAMITY_LIST = "/samity-list";
	public final static String DOWNLOAD = "/download";
	public final static String ADJUST_LOAN = "/adjust-loan";
	public final static String AUTHORIZE = "/authorize";
	public final static String UNAUTHORIZE = "/unauthorize";
	public final static String REJECT = "/reject";
	public static final String START = "/start";
	public static final String RETRY = "/retry";
	public static final String RETRY_ALL = "/retry-all";
	public static final String FILTER = "/filter";
	public static final String FORWARD_DAY_ROUTINE = "/forward-day-routine";
	public static final String CANCEL = "/cancel";
	public static final String UPDATE = "/update";
	public static final String ACCOUNTING = "/accounting";
	public static final String STATUS = "/status";
	public static final String BTN_CREATE_TRANSACTION = "/btn-create-transaction";
	public static final String OFFLINE = "/offline";
	public static final String MIGRATION = "/migrate";
	public static final String CUT_OFF_DATE_COLLECTION = "/cut-off-date-collection";
	public static final String AUTO_VOUCHER = "/auto-voucher";
	public static final String GENERATE = "/generate";
	public static final String GET_LOAN_AND_SAVINGS_ACCOUNT_DETAILS_BY_MEMBER = "/rebate/member-details-by-id";
	public static final String SETTLE_REBATED_AMOUNT = "/rebate/by-account/collect";
	public static final String LOAN_REBATE_GRID_VIEW_BY_OFFICE = "/rebate/by-office/grid-view";
	public static final String SUBMIT_LOAN_REBATE = "/rebate/submit";
	public static final String RESET_LOAN_REBATE = "/rebate/reset";
	public static final String LOAN_REBATE_DETAIL_VIEW_BY_ID = "/rebate/by-id/details";
	public static final String UPDATE_LOAN_REBATE = "/rebate/by-account/update";
	public static final String DELETE_ALL_OFFICE_DATA = "/delete-all-office-data";

	// Loan Calculator
	public static final String LOAN_CALCULATOR_BASE_URL = "/loan-calculator";
	public static final String LOAN_CALCULATOR_LOAN_PRODUCTS = "/loan-products";
	public static final String LOAN_CALCULATOR_LOAN_PRODUCT_INFO = "/loan-product-details";
	public static final String LOAN_CALCULATOR_REPAYMENT_SCHEDULE = "/repayment-schedule";

	public final static String LOAN_WAIVER = "/waiver";
	public static final String BY_ID = "/by-id";
	public static final String MEMBER_DETAILS_BY_ID = "/member-details-by-id";

	public static final String ACTIVATE_SAVINGS_ACCOUNT = "/savings-account/activate";
	public static final String RESET_COLLECTION = "/reset-collection";

	public static final String DAY_FORWARD_ROUTINE = "/day-forward-routine/start";
	public static final String DAY_FORWARD_GRID_VIEW = "/day-forward-routine/grid-view";
	public static final String DAY_FORWARD_CONFIRM = "/day-forward-routine/confirm";
	public static final String RETRY_DAY_FORWARD = "/day-forward-routine/retry";
	public static final String BY_OID = "/by-oid";
	public static final String RESET_SPECIAL_COLLECTION = "/reset-special-collection";


	// Seasonal Loan
	public static final String SEASONAL_LOAN_BASE_URL = "/seasonal-loan";
	public static final String COLLECTION = "/collection";

	public static final String RESET_STAGING_PROCESS = "/reset-staging-process";
	public static final String RESET_DAY_FORWARD_PROCESS = "/reset-day-forward-process";

	public static final String META_PROPERTY = "/meta-property";
	public static final String TRANSACTION_ADJUSTMENT = "/transaction-adjustment";

}
