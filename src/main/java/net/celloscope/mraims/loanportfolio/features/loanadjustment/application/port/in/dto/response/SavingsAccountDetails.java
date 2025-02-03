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
public class SavingsAccountDetails {

    private String savingsAccountId;
    private String savingsTypeId;
    private String savingsProductId;
    private String savingsProductNameEn;
    private String savingsProductNameBn;

    private BigDecimal balance;
    private BigDecimal availableBalance;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
