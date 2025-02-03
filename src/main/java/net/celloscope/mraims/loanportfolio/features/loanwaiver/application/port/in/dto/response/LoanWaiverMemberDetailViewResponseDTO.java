package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWaiverMemberDetailViewResponseDTO {

    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private List<LoanAccountDetails> loanAccountList;
    private List<SavingsAccountDetails> savingsAccountList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
