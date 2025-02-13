package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.out.gateway.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("template.staging_account_data")
public class StagingAccountDataEntity extends BaseToString {

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
    private BigDecimal lastDepositAmount;
    private BigDecimal totalDeposit;
    private BigDecimal totalWithdraw;
    private BigDecimal accruedInterestAmount;
    private String depositSchemeDetail;
    private LocalDateTime createdOn;
    private String createdBy;
}



