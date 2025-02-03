package net.celloscope.mraims.loanportfolio.features.autovoucher.domain;

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
public class AutoVoucher {
    private String oid;
    private String managementProcessId;
    private String processId;
    private String voucherId;
    private String voucherType;
    private String voucherNameEn;
    private String voucherNameBn;
    private LocalDate voucherDate;
    private BigDecimal voucherAmount;
    private String voucherPreparedBy;
    private String remarks;
    private String voucherDocId;
    private String ledgerId;
    private String receivedMode;
    private String subledgerId;
    private String officeId;
    private String mfiId;
    private String signatureId;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String isVisible;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String aisRequest;
    private String archivedBy;
    private LocalDateTime archivedOn;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
