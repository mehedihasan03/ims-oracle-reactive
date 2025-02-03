package net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAdjustment {
    private String oid;
    private String managementProcessId;
    private LocalDate businessDate;
    private String memberId;
    private String officeId;
    private String mfiId;
    private String loanAccountId;
    private String loanProductId;
    private String savingsAccountId;
    private String savingsProductId;
    private String transactionId;
    private LocalDate transactionDate;
    private String reverseTransactionId;
    private LocalDate reverseTransactionDate;
    private BigDecimal reverseTransactionAmount;
    private String adjustmentTransactionId;
    private LocalDate adjustmentTransactionDate;
    private BigDecimal adjustmentTransactionAmount;
    private BigDecimal transactionAmount;
    private BigDecimal netAmount;
    private String paymentMode;
    private String journalEntryType;
    private String remarks;
    private String status;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private BigDecimal transactionPrincipal;
    private BigDecimal transactionServiceCharge;
    private String transactionCode;
    private String referenceId;
    private String savingsTypeId;

    // helpers
    private String stagingDataId;
    private String loginId;
    private String savingsTransactionType;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
