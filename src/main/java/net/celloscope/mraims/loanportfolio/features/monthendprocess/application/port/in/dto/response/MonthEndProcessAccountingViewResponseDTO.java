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
public class MonthEndProcessAccountingViewResponseDTO {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;
    private String status;
    private String isFinancialPeriodAvailable;
    private String userMessage;

    private String btnConfirmEnabled;
    private String btnRefreshEnabled;

    private InterestAccruedResponse interestAccrued;
    private InterestPostingResponse interestPosting;

    private List<String> transactionCodeList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
