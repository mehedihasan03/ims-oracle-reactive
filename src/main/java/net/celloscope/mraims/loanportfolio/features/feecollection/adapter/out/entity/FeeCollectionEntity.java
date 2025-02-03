package net.celloscope.mraims.loanportfolio.features.feecollection.adapter.out.entity;

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
import java.util.UUID;

import static java.util.Objects.isNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("fee_collection")
public class FeeCollectionEntity implements Persistable<String> {

    @Id
    private String oid;
    private String feeCollectionId;
    private String managementProcessId;
    private String referenceNo;
    private LocalDate collectionDate;
    private BigDecimal amount;
    private String receiveMode;
    private String debitLedgerId;
    private String debitSubledgerId;
    private String creditLedgerId;
    private String creditSubledgerId;
    private String remarks;
    private String memberId;
    private String feeCollectionCode;
    private String feeTypeSettingId;
    private String collectedBy;
    private String officeId;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;


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

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
