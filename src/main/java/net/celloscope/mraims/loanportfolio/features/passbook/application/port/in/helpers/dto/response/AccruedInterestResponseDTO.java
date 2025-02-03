package net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.helpers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccruedInterestResponseDTO {
    private String savingsAccountId;
    private BigDecimal savgAcctBeginBalance;
    private BigDecimal savgAcctEndingBalance;
    private BigDecimal savingsAvailableBalance;
    private BigDecimal totalAccruedInterDeposit;
    private LocalDate transactionDate;
    private BigDecimal totalDepositAmount;
    private BigDecimal depositAmount;

}
