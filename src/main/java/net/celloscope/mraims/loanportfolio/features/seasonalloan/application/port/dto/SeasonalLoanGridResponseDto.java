package net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.migration.components.member.Member;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SeasonalLoanGridResponseDto {
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;
    private List<SeasonalLoanData> data;
    private long totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
