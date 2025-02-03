package net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionAdjustmentRequestDto {
    private String mfiId;
    @NotEmpty(message = "member id is required!")
    private String memberId;
    @NotEmpty(message = "office id is required!")
    private String officeId;
    private String savingsAccountId;
    private String loanAccountId;
    @NotEmpty(message = "adjustment transaction amount is required!")
    private BigDecimal adjustmentTransactionAmount;
    @NotEmpty(message = "adjustment transaction date is required!")
    private LocalDate adjustmentTransactionDate;
    private String transactionId;

    @NotEmpty(message = "payment mode is required!")
    private String paymentMode;
    @NotEmpty(message = "Remarks is required!")
    private String remarks;
    private String savingsTransactionType;

    private String loginId;
}
