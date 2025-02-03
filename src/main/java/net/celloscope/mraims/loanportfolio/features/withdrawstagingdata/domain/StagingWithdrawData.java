package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingWithdrawData {

    private String stagingWithdrawDataId;
    private String managementProcessId;
    private String processId;
    private String stagingDataId;

    private String samityId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private String withdrawType;

    private String isUploaded;
    private LocalDateTime uploadedOn;
    private String uploadedBy;

    private String status;
    private String remarks;

    private LocalDateTime createdOn;
    private String createdBy;

    private String isNew;
    private Integer currentVersion;
    private LocalDateTime updatedOn;
    private String updatedBy;

    private String isSubmitted;
    private LocalDateTime submittedOn;
    private String submittedBy;

    private String isLocked;
    private LocalDateTime lockedOn;
    private String lockedBy;

    private LocalDateTime approvedOn;
    private String approvedBy;

    private LocalDateTime rejectedOn;
    private String rejectedBy;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
