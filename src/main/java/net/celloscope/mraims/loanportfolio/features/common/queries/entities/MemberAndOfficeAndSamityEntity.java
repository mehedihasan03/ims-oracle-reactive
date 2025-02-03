package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberAndOfficeAndSamityEntity {
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String mobile;
    private String mfiId;
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
}
