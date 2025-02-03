package net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.MemberEntity;
import net.celloscope.mraims.loanportfolio.features.seasonalloan.domain.SeasonalLoan;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeasonalLoanDetailViewDto {
    private String userMessage;
    private SeasonalLoan data;
    private MemberEntity memberInformation;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
