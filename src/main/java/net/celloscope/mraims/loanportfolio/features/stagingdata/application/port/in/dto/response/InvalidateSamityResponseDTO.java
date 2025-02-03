package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;


import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvalidateSamityResponseDTO {

    private List<String> samityIdList;
    private String userMessage;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
