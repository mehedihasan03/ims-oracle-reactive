package net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SamityListDTO {

    private List<AuthorizationTabViewSamityDTO> data;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
