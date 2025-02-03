package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataSamityStatusForFieldOfficerDTO {

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private String samityType;
    private Integer totalMember;
    private Integer totalMemberStaged;
    private Integer totalAccount;
    private Integer totalAccountStaged;
    private String status;
    private String remarks;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
