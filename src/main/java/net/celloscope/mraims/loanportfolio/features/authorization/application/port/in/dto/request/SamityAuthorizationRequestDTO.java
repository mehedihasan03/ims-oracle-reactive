package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamityAuthorizationRequestDTO {

    private String managementProcessId;
    private String authorizationProcessId;
    private String transactionProcessId;
    private String passbookProcessId;
    private String mfiId;
    private String loginId;
    private String officeId;
    private String samityId;
    private String source;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
