package net.celloscope.mraims.loanportfolio.features.writeoffcollection.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@EqualsAndHashCode(callSuper = true)
public class LoanWriteOffCollection {

    private String oid;
    private String loanWriteOffCollectionOid;
    private String loanWriteOffCollectionDataId;
    private String managementProcessId;
    private String processId;
    private String samityId;
    private String loanAccountId;
    private String memberId;
    private BigDecimal writeOffCollectionAmount;
    private String paymentMode;
    private String loanInfo;
    private LocalDateTime submittedOn;
    private String submittedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private String isNew;
    private int currentVersion;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private LocalDate loanWriteOffCollectionDate;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String isSubmitted;
    private String isLocked;
    private String lockedBy;
    private LocalDateTime lockedOn;
    private String remarks;
    private String rejectedBy;
    private LocalDateTime rejectedOn;
    private WriteOffPaymentInfo paymentInfo;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
