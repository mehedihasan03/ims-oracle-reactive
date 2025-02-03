package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWaiverMemberDetailViewRequestDTO extends GenericLoanWaiverRequestDTO{

/*    private String mfiId;
    private String loginId;
    private String userRole;
    private String instituteOid;*/
    private String memberId;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
