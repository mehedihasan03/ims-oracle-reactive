package net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosureDetailView;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DPSClosureDetailViewResponse {
    private String userMessage;
    private DPSClosureDetailView data;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }

}
