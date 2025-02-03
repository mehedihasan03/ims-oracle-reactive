package net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanProductInfoResponseDTO {
    private LoanProductDTO loanProductDTO;
    private ServiceChargeInfoDTO serviceChargeInfoDTO;
    private String userMessage;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
