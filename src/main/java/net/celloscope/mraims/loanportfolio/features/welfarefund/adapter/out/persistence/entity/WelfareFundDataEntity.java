package net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.out.persistence.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.welfare_fund_data")
public class WelfareFundDataEntity implements Persistable<String> {

    @Id
    private String oid;
    private String welfareFundDataId;
    private String managementProcessId;
    private String officeId;
    private String loanAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private String referenceNo;
    private String samityId;
    private String isNew;
    private int currentVersion;
    private String status;
    private LocalDate transactionDate;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String remarks;
    private LocalDateTime approvedOn;
    private String approvedBy;
    private LocalDateTime rejectedOn;
    private String rejectedBy;


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

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
