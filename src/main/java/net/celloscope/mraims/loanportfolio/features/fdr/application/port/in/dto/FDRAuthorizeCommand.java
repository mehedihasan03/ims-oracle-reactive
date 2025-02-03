package net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FDRAuthorizeCommand {
    private String savingsAccountId;
    private String loginId;
    private String mfiId;
    private String officeId;
    private String remarks;
}
