package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeProcessDTO {
	
	private String managementProcessId;
	private String officeId;
	private String officeNameEn;
	private String officeNameBn;
	
	private LocalDate businessDate;
	private String businessDay;
	private Integer daysLagging;
	
	private String isDayStarted;
	private String isStagingDataGenerated;
	private String isDayEndProcessCompleted;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
