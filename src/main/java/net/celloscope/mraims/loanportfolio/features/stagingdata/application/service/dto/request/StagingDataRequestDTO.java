package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.request;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataRequestDTO extends BaseToString {

    private String officeId;
    private String samityId;
    private String fieldOfficerId;
    private String mfiId;
    private String loginId;
    private String accountId;
    private String institutionId;
}
