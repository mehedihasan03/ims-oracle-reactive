package net.celloscope.mraims.loanportfolio.features.rebate.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.CollectionDetailView;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedLoanAccount;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RebatePaymentInfo {
    private CollectionDetailView collection;
    private AdjustedLoanAccount adjustment;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
