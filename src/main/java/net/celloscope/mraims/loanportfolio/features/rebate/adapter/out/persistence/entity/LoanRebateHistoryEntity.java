package net.celloscope.mraims.loanportfolio.features.rebate.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
@Table("loan_rebate_data_history")
public class LoanRebateHistoryEntity implements Persistable<String> {
    @Id
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
    private String archivedBy;
    private LocalDateTime archivedOn;
    private String paymentInfo;


    @Override
    public String getId() {
        return this.oid;
    }

    @Override
    public boolean isNew() {
        boolean isNull = Objects.isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }
}
