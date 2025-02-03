package net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataOfflineResponseDTO {

    private String userMessage;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
