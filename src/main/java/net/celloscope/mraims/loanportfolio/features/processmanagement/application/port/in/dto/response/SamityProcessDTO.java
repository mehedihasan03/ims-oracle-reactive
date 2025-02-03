package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamityProcessDTO {
	
	private String samityId;
	private String samityNameEn;
	private String samityNameBn;
	private String samityDay;
	
	private String fieldOfficerId;
	private String fieldOfficerNameEn;
	private String fieldOfficerNameBn;

	private String collectionType;
	private String isCollectionDownloaded;
	private String isCollectionUploaded;

	private String isCollectionCompleted;
	private String isCollectionAuthorizationCompleted;
	private String isCollectionTransactionCompleted;
	private String isCollectionPassbookCompleted;

	private String withdrawType;
	private String isWithdrawDownloaded;
	private String isWithdrawUploaded;

	private String isWithdrawCompleted;
	private String isWithdrawAuthorizationCompleted;
	private String isWithdrawTransactionCompleted;
	private String isWithdrawPassbookCompleted;

	private String isCanceled;
	private String remarks;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
