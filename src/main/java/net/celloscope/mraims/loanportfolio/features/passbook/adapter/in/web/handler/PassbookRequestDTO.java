package net.celloscope.mraims.loanportfolio.features.passbook.adapter.in.web.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PassbookRequestDTO {
    private BigDecimal amount;
    private String managementProcessId;
    private String processId;
    private String loanAccountId;
    private String savingsAccountId;
    private String transactionId;
    private String transactionCode;
    private String mfiId;
    private String officeId;
    private String loginId;
    private LocalDate transactionDate;
    private String accruedInterDepositId;
    private String paymentMode;

    private BigDecimal calculatedServiceCharge;
    private String memberId;
    private String loanAdjustmentProcessId;

    private BigDecimal savgAcctBeginBalance;
    private BigDecimal totalDepositAmount;
    private BigDecimal totalAccruedInterDeposit;

    private String loanAccountOid;
    private String source;
    private String samityId;

}
