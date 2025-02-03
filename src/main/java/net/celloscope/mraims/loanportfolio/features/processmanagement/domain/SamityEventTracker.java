package net.celloscope.mraims.loanportfolio.features.processmanagement.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamityEventTracker {

	private String oid;
	private String managementProcessId;
	private String samityEventTrackerId;
	private String officeId;
	private String samityId;
	private String samityEvent;
	private String remarks;
	private LocalDateTime createdOn;
	private String createdBy;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
