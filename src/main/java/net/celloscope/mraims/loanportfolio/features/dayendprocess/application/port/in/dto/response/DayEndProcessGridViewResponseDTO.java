package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper.DayEndProcessSamityResponse;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEndProcessGridViewResponseDTO {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;
    private String status;
    private String btnStartEnabled;
    private String btnDeleteEnabled;
    private String btnDetailsEnabled;
    private String isFinancialPeriodAvailable;
    private String userMessage;
    private List<DayEndProcessSamityResponse> data;
    private Integer totalCount;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
