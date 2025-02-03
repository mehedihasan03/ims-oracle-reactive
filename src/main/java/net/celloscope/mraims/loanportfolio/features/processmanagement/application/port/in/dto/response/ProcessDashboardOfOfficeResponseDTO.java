package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDashboardOfOfficeResponseDTO {
	
	private String mfiId;
	private List<OfficeProcessDTO> data;
	private Long totalCount;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
