package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.request;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationRequestDTO {

    private String mfiId;
    private String loginId;
    private String officeId;
    private String fieldOfficerId;
    private String samityId;
    private List<String> samityIdList;
    private Integer limit;
    private Integer offset;
    private String source;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
