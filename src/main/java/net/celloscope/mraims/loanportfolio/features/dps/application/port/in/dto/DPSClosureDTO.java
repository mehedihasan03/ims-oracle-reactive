package net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.features.dps.domain.DPSClosure;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DPSClosureDTO {
    private String userMessage;
    private DPSClosure data;
}
