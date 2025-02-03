package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;


import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCollectionByFieldOfficerCommand {
	public String mfiId;
	public String loginId;
	private String officeId;
	public String fieldOfficerId;

	private List<CollectionDataForFieldOfficer> data;
	
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
