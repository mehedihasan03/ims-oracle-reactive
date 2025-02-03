package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StagingDataHistory extends BaseToString {
    private String stagingDataId;
    private String processId;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String mobile;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private String mfiId;
    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;
    private Integer totalMember;
    private LocalDateTime downloadedOn;
    private String downloadedBy;
    private LocalDateTime createdOn;
    private String createdBy;
}
