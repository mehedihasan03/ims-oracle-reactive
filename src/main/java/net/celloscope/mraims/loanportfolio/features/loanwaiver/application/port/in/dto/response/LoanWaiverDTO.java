package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanWaiverDTO {
    private String oid;
    private String loanWaiverDataId;
    private String managementProcessId;
    private String processId;

    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String loanAccountId;
    private BigDecimal loanAmount;
    private LocalDate loanWaiverDate;
    private BigDecimal waivedAmount;
    private BigDecimal payableAmount;
    private String status;
    private String btnUpdateEnabled;
    private String btnSubmitEnabled;

    private String paymentMode;
    private LocalDateTime submittedOn;
    private String submittedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private String isNew;
    private Integer currentVersion;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String isSubmitted;
    private String isLocked;
    private String lockedBy;
    private LocalDateTime lockedOn;
    private String remarks;
    private String rejectedBy;
    private LocalDateTime rejectedOn;
}
