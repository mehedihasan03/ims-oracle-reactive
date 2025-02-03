package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class LockCollectionResponseDto {
    private String userMessage;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
