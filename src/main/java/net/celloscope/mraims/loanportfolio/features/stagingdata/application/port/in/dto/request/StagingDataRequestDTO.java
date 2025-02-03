package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.request;

import io.lettuce.core.protocol.CommandHandler;
import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataRequestDTO {

    private String instituteOid;
    private String mfiId;
    private String loginId;
    private String officeId;
    private String samityId;
    private String fieldOfficerId;
    private String memberId;
    private String accountId;
    private String employeeId;
    private String userRole;
    private List<String> samityIdList;
    private String remarks;
    private Boolean isScheduledRequest;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
