package net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SeasonalLoanGridRequestDto {
    private String fieldOfficerId;
    private String loanAccountId;
    private String instituteOid;
    private String officeId;
    private String samityId;
    private String memberId;
    private int limit;
    private int offset;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
