package net.celloscope.mraims.loanportfolio.features.autovoucher.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoVoucherData {
    private String description;
    private BigDecimal debitedAmount;
    private BigDecimal creditedAmount;
    private String ledgerId;
    private String subledgerId;
}
