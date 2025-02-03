package net.celloscope.mraims.loanportfolio.features.calendar.domain;


import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday {
	
	public String holidayId;
	public String calendarDayId;
	public String officeId;
	public LocalDate holidayDate;
	public String holidayType;
	public String titleEn;
	public String titleBn;
	public String mfiId;
	public String status;
	public String managementProcessId;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
