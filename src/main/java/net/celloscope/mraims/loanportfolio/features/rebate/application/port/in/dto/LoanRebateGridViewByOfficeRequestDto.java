package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanRebateGridViewByOfficeRequestDto {
    private String mfiId;
    private String loginId;
    private String userRole;
    private String instituteOid;
    private String officeId;
    private String samityId;
    private String status;
    private int limit;
    private int offset;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
