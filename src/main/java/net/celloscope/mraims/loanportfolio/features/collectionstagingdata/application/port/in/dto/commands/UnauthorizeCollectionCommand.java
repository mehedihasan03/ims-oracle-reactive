package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnauthorizeCollectionCommand {

    private String managementProcessId;
    private String mfiId;
    private String officeId;
    private String loginId;
    private String fieldOfficerId;
    private String samityId;
    private List<String> stagingDataIdList;
    private List<String> transactionIdList;
    private List<String> loanRepayScheduleIdList;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
