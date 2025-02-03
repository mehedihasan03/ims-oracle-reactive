package net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SplitTransactionDTO {
    BigDecimal loanAmount;
    BigDecimal installmentAmount;
    BigDecimal beginPrinBalance;
    BigDecimal serviceChargeRatePerPeriod;
}
