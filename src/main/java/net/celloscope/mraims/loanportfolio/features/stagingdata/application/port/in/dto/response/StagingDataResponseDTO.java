package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StagingDataResponseDTO {
    private String processId;
    private String memberId;
    private String mfiId;
    private String stagingDataId;
    private String userMessage;
    private String samityId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
