package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.loan_adjustment_data_history")
public class LoanAdjustmentDataHistoryEntity implements Persistable<String> {

    @Id
    private String oid;
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

    private LocalDateTime archivedOn;
    private String archivedBy;

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
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
