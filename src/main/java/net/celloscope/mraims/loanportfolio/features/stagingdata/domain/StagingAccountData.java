package net.celloscope.mraims.loanportfolio.features.stagingdata.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingAccountData extends BaseToString {

    private String stagingAccountDataId;
    private String managementProcessId;
    private String processId;

    private String memberId;
    
//    Loan Account
    private String oid;
    private String loanAccountId;
    private String productCode;
    private String productNameEn;
    private String productNameBn;
    
    private BigDecimal loanAmount;
    private BigDecimal serviceCharge;
    private List<Installment> installments;
    
    private BigDecimal totalDue;
    private BigDecimal totalAdvance;
    private BigDecimal totalPrincipalPaid;
    private BigDecimal totalPrincipalRemaining;
    private BigDecimal totalServiceChargePaid;
    private BigDecimal totalServiceChargeRemaining;
    private LocalDate disbursementDate;

//    savings account
    private String savingsAccountId;
    private String savingsProductCode;
    private String savingsProductNameEn;
    private String savingsProductNameBn;
    private String savingsProductType;
    
    private BigDecimal targetAmount;
    private BigDecimal balance; //savings account ending balance (not available balance)
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
    private List<DepositSchemeDetailDTO> depositSchemeDetail;

    private String createdBy;
    private LocalDateTime createdOn;

    private boolean eligibleToStage;
    private Integer dpsPendingInstallmentNo;
    private BigDecimal scheduledInstallmentAmount;
}
