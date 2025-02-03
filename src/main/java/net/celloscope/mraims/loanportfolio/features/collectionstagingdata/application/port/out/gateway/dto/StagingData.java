package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.out.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StagingData {
    private String oid;
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

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
