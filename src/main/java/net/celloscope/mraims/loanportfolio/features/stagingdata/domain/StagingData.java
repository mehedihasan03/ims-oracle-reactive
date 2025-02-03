package net.celloscope.mraims.loanportfolio.features.stagingdata.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingData {

    private String managementProcessId;
    private String processId;
    private String stagingDataId;
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

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;

    private String mfiId;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private Integer totalMember;

    private String downloadedBy;
    private LocalDateTime downloadedOn;

    private String createdBy;
    private LocalDateTime createdOn;

    private String updatedBy;
    private String updatedOn;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }

}
