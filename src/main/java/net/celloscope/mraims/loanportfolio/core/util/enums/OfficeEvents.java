package net.celloscope.mraims.loanportfolio.core.util.enums;

import lombok.Getter;

@Getter
public enum OfficeEvents {
	
	DAY_STARTED("DayStarted"),
//	STAGED("Staged"),
	STAGED("StagingDataGenerationCompleted"),
	STAGING_DATA_GENERATION_STARTED("StagingDataGenerationStarted"),
	STAGING_DATA_GENERATION_COMPLETED("StagingDataGenerationCompleted"),
	AUTO_VOUCHER_GENERATION_STARTED("AutoVoucherGenerationStarted"),
	AUTO_VOUCHER_GENERATION_COMPLETED("AutoVoucherGenerationCompleted"),
	DAY_END_PROCESS_STARTED("DayEndProcessStarted"),
	DAY_END_PROCESS_COMPLETED("DayEndProcessCompleted"),
	MONTH_END_PROCESS_COMPLETED("MonthEndProcessCompleted"),
	FORWARD_DAY_ROUTINE_COMPLETED("ForwardDayRoutineCompleted");
	
	private final String value;
	
	OfficeEvents(String value) {
		this.value = value;
	}
}
