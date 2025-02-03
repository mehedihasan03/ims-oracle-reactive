package net.celloscope.mraims.loanportfolio.features.migrationV3.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfficeDataDeleteRequestDTO {
    private String officeId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
