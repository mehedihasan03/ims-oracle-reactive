package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto.MemberInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingLoanAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingSavingsAccountInfoDTO;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsInfo {

    private MemberInfoDTO memberInfo;
    private StagingLoanAccountInfoDTO loanAccountInfo;
    private StagingSavingsAccountInfoDTO savingsAccountInfo;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
