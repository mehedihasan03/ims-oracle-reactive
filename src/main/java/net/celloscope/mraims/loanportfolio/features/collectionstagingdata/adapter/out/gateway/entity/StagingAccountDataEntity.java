package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("template.staging_account_data")
public class StagingAccountDataEntity {
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
    private String savingsProductNameBn;
    private String savingsProductType;

    private BigDecimal targetAmount;
    private BigDecimal balance;
    private BigDecimal savingsAvailableBalance;
    private BigDecimal totalDeposit;
    private BigDecimal totalWithdraw;

    private BigDecimal lastDepositAmount;
    private LocalDateTime lastDepositDate;
    private String lastDepositType;

    private BigDecimal lastWithdrawAmount;
    private LocalDateTime lastWithdrawDate;
    private String lastWithdrawType;

    private BigDecimal accruedInterestAmount;
    private String depositSchemeDetail;

    private LocalDateTime createdOn;
    private String createdBy;
    private String managementProcessId;

    private String processId;

}
