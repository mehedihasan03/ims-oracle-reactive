package net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeasonalLoanCollectionRequestDto {
    private String instituteOid;
    private String officeId;
    private String loginId;
    private String mfiId;
    private String loanAccountId;
    private BigDecimal collectionAmount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
