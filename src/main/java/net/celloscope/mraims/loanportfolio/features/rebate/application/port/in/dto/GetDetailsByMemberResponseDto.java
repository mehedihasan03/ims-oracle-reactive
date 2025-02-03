package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetDetailsByMemberResponseDto {
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private List<LoanAccountForRebateDto> loanAccountList;
    private List<SavingsAccountForRebateDto> savingsAccountList;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
