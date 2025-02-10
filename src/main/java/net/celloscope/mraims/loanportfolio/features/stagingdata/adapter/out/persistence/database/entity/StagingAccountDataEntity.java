package net.celloscope.mraims.loanportfolio.features.stagingdata.adapter.out.persistence.database.entity;


import com.google.gson.annotations.Expose;
import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.staging_account_data")
public class StagingAccountDataEntity extends BaseToString implements Persistable<String> {

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
    private BigDecimal totalAdvance;
    private Integer dpsPendingInstallmentNo;
    private BigDecimal scheduledInstallmentAmount;

    

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
