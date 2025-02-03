package net.celloscope.mraims.loanportfolio.features.writeoffcollection.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedLoanAccount;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.LoanWriteOffCashCollection;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WriteOffPaymentInfo {
    private LoanWriteOffCashCollection collection;
    private AdjustedLoanAccount adjustment;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
