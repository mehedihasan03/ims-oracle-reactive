package net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDRClosure;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FDRClosureDTO {
    private String userMessage;
    private FDRClosure data;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
