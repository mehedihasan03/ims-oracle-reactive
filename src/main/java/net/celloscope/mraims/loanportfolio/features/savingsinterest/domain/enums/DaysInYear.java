package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.enums;

public enum DaysInYear {
	
	_365_FIXED("365 Fixed"),
	_360_DAYS("360 DAYS"),
	_30_360_GERMAN("30/360 GERMAN"),
	_ACTUAL_ISDA("Actual ISDA"),
	_365("365");
	
	private final String value;
	
	DaysInYear(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return this.value;
	}
}
