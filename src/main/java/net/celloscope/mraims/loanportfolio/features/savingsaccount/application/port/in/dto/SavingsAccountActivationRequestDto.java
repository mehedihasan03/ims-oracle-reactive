package net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsAccountActivationRequestDto {
    private String savingsAccountId;
    private String memberId;
    private BigDecimal openingBalance;
    private String officeId;
    private String mfiId;
    private String loginId;
    private String instituteOid;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
