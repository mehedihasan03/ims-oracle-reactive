package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavingsAccountForRebateDto {
    private String savingsAccountId;
    private String savingsProductId;
    private String savingsProductNameEn;
    private String savingsProductNameBn;
    private BigDecimal balance;
    private BigDecimal availableBalance;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
