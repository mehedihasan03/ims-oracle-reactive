package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StagingAccountData {
    private String oid;
    private String stagingAccountDataId;
    private String memberId;
    private String loanAccountId;
    private String productCode;
    private String productName;
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
    private String managementProcessId;
    private String processId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
