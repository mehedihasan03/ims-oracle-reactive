package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayEndProcessStatusResponseDTO {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;

    private String btnRunDayEndProcessEnabled;
    private String btnDeleteEnabled;

    private String status;
    private String userMessage;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
