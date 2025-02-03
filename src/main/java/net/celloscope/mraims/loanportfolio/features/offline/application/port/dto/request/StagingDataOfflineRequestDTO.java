package net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataOfflineRequestDTO {

    private String officeId;
    private String fieldOfficerId;
    private String loginId;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
