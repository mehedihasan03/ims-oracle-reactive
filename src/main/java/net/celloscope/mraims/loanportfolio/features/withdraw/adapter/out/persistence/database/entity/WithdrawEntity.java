package net.celloscope.mraims.loanportfolio.features.withdraw.adapter.out.persistence.database.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.staging_withdraw_data")
public class WithdrawEntity extends BaseToString implements Persistable<String> {
    @Id
    private String oid;
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
    public String getId() {
        return this.getOid();
    }

    @Override
    public boolean isNew() {
        boolean isNull = isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }
}
