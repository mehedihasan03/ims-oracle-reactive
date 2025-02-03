package net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPS;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DPSDetailViewDTO {
    private String userMessage;
    private DPS data;
    private String btnEncashEnabled;
}
