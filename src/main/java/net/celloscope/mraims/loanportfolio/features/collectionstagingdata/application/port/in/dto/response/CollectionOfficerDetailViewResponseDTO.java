package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response;


import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.CollectionStagingDataFieldOfficerDetailViewResponse;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionOfficerDetailViewResponseDTO {
	
	private String officeId;
	private String fieldOfficerId;
	private String fieldOfficerNameEn;
	private String fieldOfficerNameBn;
	
	private List<CollectionStagingDataFieldOfficerDetailViewResponse> samityList;
	private Integer totalCount;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
