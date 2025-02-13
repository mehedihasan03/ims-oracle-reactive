package net.celloscope.mraims.loanportfolio.features.migration.components.loanwriteoff;

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
@NoArgsConstructor
@AllArgsConstructor
@Table("template.loan_write_off_collection")
public class LoanWriteOffCollectionEntity implements Persistable<String> {
    @Id
    private String oid;
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
