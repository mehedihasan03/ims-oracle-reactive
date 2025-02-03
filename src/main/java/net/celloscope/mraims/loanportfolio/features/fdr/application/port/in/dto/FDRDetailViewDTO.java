package net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDR;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FDRDetailViewDTO {
    private String userMessage;
    private FDR data;
    private String btnEncashEnabled;
}
