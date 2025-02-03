package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.helper.WithdrawMemberInfoDTO;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawDetailViewResponseDTO {

    private String officeId;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private Integer totalMember;
    private String mfiId;

    private List<WithdrawMemberInfoDTO> memberList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
