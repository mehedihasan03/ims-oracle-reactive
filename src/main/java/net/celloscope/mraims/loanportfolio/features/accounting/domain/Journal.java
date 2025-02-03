package net.celloscope.mraims.loanportfolio.features.accounting.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Journal {
    private String description;
    private BigDecimal debitedAmount;
    private BigDecimal creditedAmount;
    private String ledgerId;
    private String subledgerId;
}
