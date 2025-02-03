package net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PassbookDTO {
    private String oid;
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
    private BigDecimal totalPayment;
    private BigDecimal penaltyPaid;
    private BigDecimal penaltyPaidTillDate;
    private BigDecimal penaltyRemain;
    private BigDecimal feesPaid;
    private BigDecimal feePaidTillDate;
    private BigDecimal feeRemain;
    private BigDecimal installmentEndPrinBalance;
    private String savingsAccountId;
    private String accruedInterDepositId;
    private BigDecimal savgAcctBeginBalance;
    private BigDecimal savgAcctEndingBalance;
    private BigDecimal savingsAvailableBalance;
    private BigDecimal lumpSumpPayment;
    private LocalDate collectWithdrawOn;
    private String mfiId;
    private LocalDateTime postedOn;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;
    private String deleted;

    private String disbursedLoanAccountId;
    private BigDecimal loanAmount;
    private BigDecimal calculatedServiceCharge;
    private LocalDate disbursementDate;

}
