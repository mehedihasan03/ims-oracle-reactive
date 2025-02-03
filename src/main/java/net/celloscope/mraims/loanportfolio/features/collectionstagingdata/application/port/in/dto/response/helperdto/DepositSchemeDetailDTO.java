package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response.helperdto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositSchemeDetailDTO {
    private String details;


    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
