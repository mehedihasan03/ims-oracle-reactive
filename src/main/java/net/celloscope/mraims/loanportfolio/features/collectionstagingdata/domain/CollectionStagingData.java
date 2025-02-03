package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CollectionStagingData {
	private String oid;
	private String managementProcessId;
	private String collectionStagingDataId;
	private String processId;
	private String samityId;
	private String stagingDataId;
	private String accountType;
	private String loanAccountId;
	private String savingsAccountId;
	private BigDecimal amount;
	private String paymentMode;
	private String collectionType;
	private LocalDateTime createdOn;
	private String createdBy;
	private LocalDateTime submittedOn;
	private String submittedBy;
	private String isUploaded;
	private LocalDateTime uploadedOn;
	private String uploadedBy;
	private LocalDateTime approvedOn;
	private String approvedBy;
	private String currentVersion;
	private String status;
	
	private String updatedBy;
	private LocalDateTime updatedOn;
	private String isLocked;
	private String lockedBy;
	private LocalDateTime lockedOn;
	private String rejectedBy;
	private LocalDateTime rejectedOn;
	private String isNew;
	private String remarks;
	
	private String editCommit;
	private String isSubmitted;
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
