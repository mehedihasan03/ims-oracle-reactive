package net.celloscope.mraims.loanportfolio.features.feecollection.application.port.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeCollectionUpdateRequestDTO {

    private String officeId;
    private String instituteOid;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
