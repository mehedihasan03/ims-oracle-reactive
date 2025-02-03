package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.AccruedInterest;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccruedInterestResponseDTO {
    private List<AccruedInterest> data;
    private Integer count;
    private String userMessage;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }

}
