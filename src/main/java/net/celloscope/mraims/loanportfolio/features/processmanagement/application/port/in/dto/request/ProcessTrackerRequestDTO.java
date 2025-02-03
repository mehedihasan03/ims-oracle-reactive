package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.request;


import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTrackerRequestDTO {
	
	private String instituteOid;
	private String mfiId;
	private String loginId;
	private String managementProcessId;
	private String businessDate;
	private String officeId;
	private String samityId;
	private String remarks;
	private Integer limit;
	private Integer offset;
	private List<String> samityIdList;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
