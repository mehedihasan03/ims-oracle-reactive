package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAdjustmentGridViewResponseDTO {

    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private List<AdjustedLoanAccount> adjustedLoanAccountList;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
