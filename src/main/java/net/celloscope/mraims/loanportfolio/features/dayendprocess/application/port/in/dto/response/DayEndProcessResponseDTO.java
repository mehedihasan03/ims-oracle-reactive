package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEndProcessResponseDTO {

    private String officeId;
    private String status;
    private String userMessage;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
