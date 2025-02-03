package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.helper;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawMemberInfoDTO {

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

    private List<WithdrawSavingsAccountInfoDTO> savingsAccountList;

    private String downloadedBy;
    private LocalDateTime downloadedOn;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
