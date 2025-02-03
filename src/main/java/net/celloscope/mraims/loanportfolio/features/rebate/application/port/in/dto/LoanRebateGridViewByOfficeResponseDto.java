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
public class LoanRebateGridViewByOfficeResponseDto {
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private List<LoanRebateGridData> data;
    private Integer totalCount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
