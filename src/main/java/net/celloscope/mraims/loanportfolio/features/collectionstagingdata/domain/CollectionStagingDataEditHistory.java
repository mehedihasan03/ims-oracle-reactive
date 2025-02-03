package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionStagingDataEditHistory {
    private String oid;
    private String collectionStagingDataId;
    private String collectionStagingDataEditHistoryId;
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

    private String updatedBy;
    private LocalDateTime updatedOn;
    private String lockedBy;
    private LocalDateTime lockedOn;
    private String rejectedBy;
    private LocalDateTime rejectedOn;
    private String remarks;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
