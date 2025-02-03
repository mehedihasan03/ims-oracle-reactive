package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileInfoDTO {
	private int contact;
	private String contactNo;
	
	@Override
	public String toString(){
		return CommonFunctions.buildGsonBuilder(this);
	}
}
