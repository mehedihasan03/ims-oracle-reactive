package net.celloscope.mraims.loanportfolio.features.authorization.application.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusVerificationDTO {
    private Boolean isSubmitted;
    private Boolean isAuthorized;
    private Boolean isUnAuthorized;
    private Boolean isLocked;

    private Boolean allSubmitted;
    private Boolean allAuthorized;
    private Boolean allUnAuthorized;
    private Boolean allLocked;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
