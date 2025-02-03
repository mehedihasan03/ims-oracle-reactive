package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.response.helper.WithdrawGridViewDataObject;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationWithdrawGridViewResponseDTO {

    private String officeId;
    private List<WithdrawGridViewDataObject> data;
    private Integer totalCount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
