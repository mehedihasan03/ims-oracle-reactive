package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorizationGridViewResponseDTO {
    private String officeId;
    private List<GridViewDataObject> data;
    private Integer totalCount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
