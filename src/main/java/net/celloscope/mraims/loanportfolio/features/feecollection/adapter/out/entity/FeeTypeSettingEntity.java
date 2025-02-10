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
@Table("template.fee_type_setting")
public class FeeTypeSettingEntity implements Persistable<String> {

    @Id
    private String oid;
    private String feeTypeSettingId;
    private String feeCollectionCode;
    private String feeTypeNameEn;
    private String feeTypeNameBn;
    private BigDecimal amount;
    private String remarks;
    private String ledgerId;
    private String subledgerId;
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
