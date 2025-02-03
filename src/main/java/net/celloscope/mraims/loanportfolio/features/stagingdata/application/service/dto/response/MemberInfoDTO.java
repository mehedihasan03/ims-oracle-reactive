package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoDTO extends BaseToString {

    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String mobile;
    private String stagingDataId;

    private List<StagingLoanAccountInfoDTO> loanAccountList;
    private List<StagingSavingsAccountInfoDTO> savingsAccountList;
}
