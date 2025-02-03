package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelfareFundSaveResponseDto {

    private String userMessage;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
