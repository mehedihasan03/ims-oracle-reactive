package net.celloscope.mraims.loanportfolio.features.rebate.domain;

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
@AllArgsConstructor
@NoArgsConstructor
public class LoanRebate {
    private String oid;
    private String loanRebateDataOid;
    private String loanRebateDataId;
    private String managementProcessId;
    private String processId;
    private String stagingDataId;
    private String samityId;
    private String loanAccountId;
    private BigDecimal rebateAmount;
    private String paymentMode;
    private LoanInfo loanInfo;
    private LocalDateTime submittedOn;
    private String submittedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private String isNew;
    private int currentVersion;
    private String status;
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
    private LocalDate earlySettlementDate;
    private BigDecimal payableAmount;
    private String memberId;
    private String editUpdate;
    private String editCommit;
    private RebatePaymentInfo paymentInfo;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
