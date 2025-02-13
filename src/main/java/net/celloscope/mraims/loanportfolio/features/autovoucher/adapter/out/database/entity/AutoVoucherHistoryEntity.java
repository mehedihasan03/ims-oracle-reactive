package net.celloscope.mraims.loanportfolio.features.autovoucher.adapter.out.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("template.auto_voucher_history")
public class AutoVoucherHistoryEntity implements Persistable<String> {

    @Id
    private String oid;
    private String autoVoucherOid;
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
    private String archivedBy;
    private LocalDateTime archivedOn;

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
