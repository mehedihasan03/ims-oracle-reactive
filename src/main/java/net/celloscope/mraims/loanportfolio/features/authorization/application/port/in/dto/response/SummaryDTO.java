package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDTO {

    private ProductListDTO loanCollectionSummary;
    private ProductListDTO savingsCollectionSummary;
    private ProductListDTO withdrawSummary;
    private ProductListDTO loanAdjustmentSummary;
    private ProductListDTO loanRebateSummary;
    private ProductListDTO loanWaiverSummary;
    private ProductListDTO loanWriteOffCollectionSummary;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
