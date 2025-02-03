package net.celloscope.mraims.loanportfolio.features.loanwaiver.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response.LoanAccountDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanWaiver {
    private String oid;
    private String loanWaiverDataId;
    private String managementProcessId;
    private String processId;
    private String samityId;
    private String loanAccountId;
    private String memberId;
    private BigDecimal waivedAmount;
    private BigDecimal payableAmount;
    private String paymentMode;
    private LocalDateTime submittedOn;
    private String submittedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private String isNew;
    private Integer currentVersion;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String isSubmitted;
    private String editCommit;
    private String isLocked;
    private String lockedBy;
    private LocalDateTime lockedOn;
    private String remarks;
    private String rejectedBy;
    private LocalDateTime rejectedOn;
    private LocalDate loanWaiverDate;
    private LoanAccountDetails loanInfo;

    private String stagingDataId;


}
