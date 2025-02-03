package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessTrackerResponseDTO {

	private String managementProcessId;
	private String officeId;
	private String officeNameEn;
	private String officeNameBn;
	private LocalDate businessDate;
	private String businessDay;
	private String btnCreateTransactionEnabled;
	private List<String> samityIdList;
	private String userMessage;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
