package net.celloscope.mraims.loanportfolio.features.processmanagement.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagementProcessTracker {
	
	private String managementProcessId;
	private String officeId;
	private String officeNameEn;
	private String officeNameBn;
	private String mfiId;
	private LocalDate businessDate;
	private String businessDay;
	private LocalDateTime createdOn;
	private String createdBy;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
