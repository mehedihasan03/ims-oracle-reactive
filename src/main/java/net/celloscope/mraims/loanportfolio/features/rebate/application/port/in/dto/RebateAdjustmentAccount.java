package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RebateAdjustmentAccount {
    private String savingsAccountId;
    private String amount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
