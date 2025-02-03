package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedSavingsAccount;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenericLoanWaiverRequestDTO {

    private String mfiId;
    private String loginId;
    private String userRole;
    private String instituteOid;
    private String officeId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
