package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request;

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
public class AdjustedAccount {

    private String savingsAccountId;
    private BigDecimal amount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
