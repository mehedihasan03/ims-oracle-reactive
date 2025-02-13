package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("template.collection_staging_data")
public class CollectionStagingDataEntity implements Persistable<String> {
	@Id
	private String oid;
	private String collectionStagingDataId;
	private String managementProcessId;
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
	private String editCommit;
	private String isNew;
	private String isSubmitted;
	
	private String updatedBy;
	private LocalDateTime updatedOn;
	private String isLocked; 
	private String lockedBy;
	private LocalDateTime lockedOn;
	private String rejectedBy;
	private LocalDateTime rejectedOn;
	private String remarks;

	
	@Override
	public String getId() {
		return this.oid;
	}
	
	public void setId(String id) {
		this.oid = id;
	}
	
	@Override
	public boolean isNew() {
		boolean isNull = Objects.isNull(this.oid);
		this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
		return isNull;
	}
	
	@Override
	public String toString() {
		return CommonFunctions.buildGsonBuilder(this);
	}
}
