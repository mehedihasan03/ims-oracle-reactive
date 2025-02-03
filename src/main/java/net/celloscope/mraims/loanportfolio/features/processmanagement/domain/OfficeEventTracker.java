package net.celloscope.mraims.loanportfolio.features.processmanagement.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeEventTracker {

	private String managementProcessId;
	private String officeEventTrackerId;
	private String officeId;
	private String officeEvent;
	private LocalDateTime createdOn;
	private String createdBy;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
