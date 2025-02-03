package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SavingsInterestResponseDTO {
    private String savingsAccountId;
    private BigDecimal availableSavings;
    private LocalDate interestCalculationDate;
    private BigDecimal accruedInterest;
}
