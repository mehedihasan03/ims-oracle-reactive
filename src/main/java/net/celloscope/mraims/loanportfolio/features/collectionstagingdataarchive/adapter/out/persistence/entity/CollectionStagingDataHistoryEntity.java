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
@Table("collection_staging_data_history")
public class CollectionStagingDataHistoryEntity extends BaseToString implements Persistable<String> {

    @Id
    private String oid;

    private String collectionStagingDataId;

    private String managementProcessId;
    private String processId;

    private String samityId;
    private String stagingDataId;
    private String accountType;
    private String loanAccountId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private String collectionType;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime submittedOn;
    private String submittedBy;
    private String isUploaded;
    private LocalDateTime uploadedOn;
    private String uploadedBy;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private String currentVersion;
    private String status;
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

