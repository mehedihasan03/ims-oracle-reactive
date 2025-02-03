package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawStagingDataQueryDTO {

    private String id;
    private String instituteOid;
    private String officeId;
    private String userRole;
    private String fieldOfficerId;
    private String mfiId;
    private String loginId;
    private String samityId;
    private String memberId;
    private String accountId;
    private Integer limit;
    private Integer offset;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
