package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StagingAccountDataHistory extends BaseToString {
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
}
