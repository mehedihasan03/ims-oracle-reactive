package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthEndProcessGridViewResponseDTO {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;
    private String status;
    private String isFinancialPeriodAvailable;
    private String userMessage;

    private String btnRunMonthEndProcessEnabled;
    private String btnDetailsEnabled;
    private String btnDeleteEnabled;
    private List<MonthEndProcessHistory> data;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
