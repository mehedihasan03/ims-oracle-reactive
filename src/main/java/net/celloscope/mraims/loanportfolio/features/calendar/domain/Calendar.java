package net.celloscope.mraims.loanportfolio.features.calendar.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Calendar {
	
	private String calendarDayId;
	private String financialPeriodId;
	private LocalDate calendarDate;
	private Integer calendarYear;
	private Integer dayOfWeek;
	private Integer monthOfYear;
	private Integer dayOfMonth;
	private Integer dayOfYear;
	private String isWorkingDay;
	private String officeId;
	private String mfiId;
	private String status;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
