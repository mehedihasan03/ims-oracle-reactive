package net.celloscope.mraims.loanportfolio.features.loanadjustment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAdjustmentDataEditHistory {
    private String oid;
    private String loanAdjustmentDataEditHistoryId;
    private String loanAdjustmentDataId;
    private String managementProcessId;
    private String processId;
    private String loanAdjustmentProcessId;

    private String samityId;
    private String memberId;

    private String loanAccountId;
    private String accountType;
    private String savingsAccountId;
    private BigDecimal amount;

    private String adjustmentType;
    private String status;

    private String createdBy;
    private LocalDateTime createdOn;

    private String isNew;
    private Integer currentVersion;
    private String updatedBy;
    private LocalDateTime updatedOn;

    private String isLocked;
    private String lockedBy;
    private LocalDateTime lockedOn;

    private String isSubmitted;
    private String submittedBy;
    private LocalDateTime submittedOn;

    private String approvedBy;
    private LocalDateTime approvedOn;
    private String rejectedBy;
    private LocalDateTime rejectedOn;
    private String remarks;

    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
