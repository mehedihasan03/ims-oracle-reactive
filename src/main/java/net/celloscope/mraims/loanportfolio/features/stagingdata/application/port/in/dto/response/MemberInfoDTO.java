package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingLoanAccountInfoDTO;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response.StagingSavingsAccountInfoDTO;

import java.time.LocalDateTime;
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
    private String registerBookSerialId;
    private String companyMemberId;
    private String gender;
    private String maritalStatus;
    private String spouseNameEn;
    private String spouseNameBn;
    private String fatherNameEn;
    private String fatherNameBn;
    private String stagingDataId;

    private List<StagingLoanAccountInfoDTO> loanAccountList;
    private List<StagingSavingsAccountInfoDTO> savingsAccountList;

    private String downloadedBy;
    private LocalDateTime downloadedOn;
}
