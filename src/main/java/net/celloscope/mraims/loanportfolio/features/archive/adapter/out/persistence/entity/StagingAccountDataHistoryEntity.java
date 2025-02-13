package net.celloscope.mraims.loanportfolio.features.archive.adapter.out.persistence.entity;

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

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table("template.staging_account_data_history")
public class StagingAccountDataHistoryEntity implements Persistable<String> {

    @Id
    private String oid;
    private String stagingAccountDataId;
    private String managementProcessId;
    private String processId;
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
    private String savingsProductNameBn;
    private String savingsProductType;

    private BigDecimal targetAmount;
    private BigDecimal balance;
    private BigDecimal savingsAvailableBalance;
    private BigDecimal totalDeposit;
    private BigDecimal totalWithdraw;

    private BigDecimal lastDepositAmount;
    private LocalDate lastDepositDate;
    private String lastDepositType;

    private BigDecimal lastWithdrawAmount;
    private LocalDate lastWithdrawDate;
    private String lastWithdrawType;

    private BigDecimal accruedInterestAmount;
    private String depositSchemeDetail;

    private LocalDateTime createdOn;
    private String createdBy;

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
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
