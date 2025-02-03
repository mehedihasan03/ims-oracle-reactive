package net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataUploadByFieldOfficerRequestDTO {

    private String officeId;
    private String fieldOfficerId;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
