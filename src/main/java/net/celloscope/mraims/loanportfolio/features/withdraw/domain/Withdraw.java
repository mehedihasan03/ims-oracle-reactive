package net.celloscope.mraims.loanportfolio.features.withdraw.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Withdraw {
    private String oid;
    private String managementProcessId;
    private String processId;
    private String samityId;
    private String withdrawStagingDataId;
    private String stagingDataId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private String withdrawType;
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
    private String isNew;
    private LocalDateTime updatedOn;
    private String updatedBy;

    private String isLocked;
    private LocalDateTime lockedOn;
    private String lockedBy;

}
