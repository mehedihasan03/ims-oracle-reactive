package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDataForFieldOfficer {
	
	private String samityId;
	private List<CollectionData> collectionStagingDataList;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
