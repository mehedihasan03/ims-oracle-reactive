package net.celloscope.mraims.loanportfolio.features.passbook.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Passbook {
    private String oid;
    private String managementProcessId;
    private String processId;
    private String transactionId;
    private String transactionCode;
    private String memberId;
    private String passbookNumber;
    private String loanAccountId;
    private String loanRepayScheduleId;
    private Integer installNo;
    private LocalDate installDate;
    private LocalDate transactionDate;
    private BigDecimal installmentBeginPrinBalance;
    private BigDecimal prinPaid;
    private BigDecimal prinPaidTillDate;
    private BigDecimal prinRemainForThisInst;
    private BigDecimal serviceChargePaid;
    private BigDecimal scPaidTillDate;
    private BigDecimal scRemainForThisInst;
    private BigDecimal installmentEndPrinBalance;
    private String savingsAccountId;
    private Integer dpsInstallmentNo;
    private LocalDate dpsInstallmentDate;
    private BigDecimal depositAmount;
    private BigDecimal withdrawAmount;
    private String accruedInterDepositId;
    private BigDecimal savgAcctBeginBalance;
    private BigDecimal savgAcctEndingBalance;
    private BigDecimal savingsAvailableBalance;
    private BigDecimal lumpSumpPayment;
    private LocalDate collectWithdrawOn;
    private BigDecimal totalDepositAmount;
    private BigDecimal totalWithdrawAmount;
    private BigDecimal totalAccruedInterDeposit;
    private String mfiId;
    private BigDecimal penaltyPaid;
    private BigDecimal penaltyPaidTillDate;
    private BigDecimal penaltyRemain;
    private BigDecimal feesPaid;
    private BigDecimal feePaidTillDate;
    private BigDecimal feeRemain;
    private LocalDateTime postedOn;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;
    private String deleted;
    // private String processManagementId;
    private String paymentMode;

    private String disbursedLoanAccountId;
    private BigDecimal loanAmount;
    private BigDecimal calculatedServiceCharge;
    private LocalDate disbursementDate;

    // newly added
    private String loanAccountOid;
    private String savingsAccountOid;

    private String loanAdjustmentProcessId;
    private String officeId;

    private String welfareFundLoanAccountId;
    private BigDecimal welfareFundAmount;
    private String referenceId;
    private String savingsTypeId;
    private String source;
    private String samityId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
