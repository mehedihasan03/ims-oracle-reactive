package net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosureGridView;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DPSClosureGridViewResponse {

    private String userMessage;
    private List<DPSClosureGridView> data;
    private Integer totalCount;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }


}
