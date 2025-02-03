package net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DayForwardProcessRequestDto {
    private String instituteOid;
    private String mfiId;
    private String loginId;
    private String managementProcessId;
    private String businessDate;
    private String officeId;
    private String samityId;
    private String remarks;
    private Integer offset;
    private Integer limit;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
