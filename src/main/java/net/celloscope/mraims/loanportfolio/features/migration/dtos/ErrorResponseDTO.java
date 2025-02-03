package net.celloscope.mraims.loanportfolio.features.migration.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {

    private String personId;
    private String message;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
