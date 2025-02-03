package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.persistence.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("staging_withdraw_data_history")
public class WithdrawStagingDataHistoryEntity extends BaseToString implements Persistable<String> {

    @Id
    private String oid;

    private String withdrawStagingDataId;
    private String stagingDataId;
    private String samityId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private String withdrawType;
    private LocalDateTime submittedOn;
    private String submittedBy;
    private String isUploaded;
    private LocalDateTime uploadedOn;
    private String uploadedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private String currentVersion;
    private String status;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String isSubmitted;

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

