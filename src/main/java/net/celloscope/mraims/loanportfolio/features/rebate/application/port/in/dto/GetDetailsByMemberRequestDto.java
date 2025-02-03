package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetDetailsByMemberRequestDto {
    private String mfiId;
    private String loginId;
    private String userRole;
    private String instituteOid;
    private String memberId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
