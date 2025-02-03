package net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDRClosureGridView;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FDRClosureGridViewResponse {
    private String userMessage;
    private List<FDRClosureGridView> data;
    private Integer totalCount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
