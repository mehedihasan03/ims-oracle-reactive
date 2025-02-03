package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustedSavingsAccount {

    private String savingsAccountId;
    private BigDecimal amount;
    private BigDecimal savingsAvailableBalance;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
