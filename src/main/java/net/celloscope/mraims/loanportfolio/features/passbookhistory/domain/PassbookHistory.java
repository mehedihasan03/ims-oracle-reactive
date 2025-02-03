package net.celloscope.mraims.loanportfolio.features.passbookhistory.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PassbookHistory {
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
    private String mfiId;
    private String status;
    private LocalDate transactionDate;
    private String deleted;
    private String paymentMode;
    private String disbursedLoanAccountId;
    private BigDecimal loanAmount;
    private BigDecimal calculatedServiceCharge;
    private LocalDate disbursementDate;
    private String loanAccountOid;
    private String savingsAccountOid;
    private String officeId;
    private String loanAdjustmentProcessId;
    private String savingsTypeId;
    private String referenceId;
    private String welfareFundLoanAccountId;
    private BigDecimal welfareFundAmount;
    private LocalDateTime archivedOn;
    private String archivedBy;
}
