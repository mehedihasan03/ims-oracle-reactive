package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response;

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
public class DayEndProcessRetryResponseDTO {

    private String officeId;
    private String officeNameEn;
    private String officeNameBn;

    private LocalDate businessDate;
    private String businessDay;

    private String userMessage;
    private List<String> transactionCodeList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
