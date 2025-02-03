package net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CancelSamityRequestDTO {
    private String instituteOid;
    private String mfiId;
    private String loginId;
    private String officeId;
    private String samityId;
    private String managementProcessId;
    private String remarks;
}
