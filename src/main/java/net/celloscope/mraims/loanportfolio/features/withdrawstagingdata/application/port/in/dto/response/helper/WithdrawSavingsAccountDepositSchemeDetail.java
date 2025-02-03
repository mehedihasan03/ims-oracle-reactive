package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.helper;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawSavingsAccountDepositSchemeDetail {
    private String details;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
