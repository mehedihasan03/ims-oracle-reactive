package net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DPSGridViewCommand {
    private String officeId;
    private String mfiId;
    private String loginId;
    private String searchText;
    private Integer offset;
    private Integer limit;
}
