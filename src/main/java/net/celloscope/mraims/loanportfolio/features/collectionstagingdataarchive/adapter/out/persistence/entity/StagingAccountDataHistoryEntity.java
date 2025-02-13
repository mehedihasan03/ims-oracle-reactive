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
@Table("template.staging_account_data_history")
public class StagingAccountDataHistoryEntity extends BaseToString implements Persistable<String> {

    @Id
    private String oid;

    private String stagingAccountDataId;
    private String memberId;
    private String loanAccountId;
    private String productCode;
    private String productNameEn;
    private String productNameBn;
    private BigDecimal loanAmount;
    private BigDecimal serviceCharge;
    private String installments;
    private BigDecimal totalDue;
    private BigDecimal totalPrincipalPaid;
    private BigDecimal totalPrincipalRemaining;
    private BigDecimal totalServiceChargePaid;
    private BigDecimal totalServiceChargeRemaining;
    private String savingsAccountId;
    private String savingsProductCode;
    private String savingsProductNameEn;
    private String savingsProductType;
    private BigDecimal targetAmount;
    private BigDecimal balance;
    private BigDecimal lastDepositAmount;
    private BigDecimal totalDeposit;
    private BigDecimal totalWithdraw;
    private BigDecimal accruedInterestAmount;
    private String depositSchemeDetail;
    private LocalDateTime createdOn;
    private String createdBy;
    private String managementProcessId;
    private String processId;

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
