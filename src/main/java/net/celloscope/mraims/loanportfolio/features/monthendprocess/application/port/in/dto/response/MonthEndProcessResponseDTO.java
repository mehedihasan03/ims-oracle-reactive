package net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthEndProcessResponseDTO {

    private String btnRunMonthEndProcessEnabled;
    private String btnRetryMonthEndProcessEnabled;
    private String officeId;
    private String calendarMonth;
    private Integer calendarYear;
    private String status;
    private String remarks;
    private String userMessage;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
