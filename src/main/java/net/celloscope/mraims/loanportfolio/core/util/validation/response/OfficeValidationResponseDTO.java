package net.celloscope.mraims.loanportfolio.core.util.validation.response;

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
public class OfficeValidationResponseDTO {

    private String managementProcessId;
    private String officeId;
    private String officeNameEn;
    private String officeNameBn;
    private LocalDate businessDate;
    private String businessDay;
    private List<String> officeEventList;
    private Boolean isDayStarted;
    private Boolean isStagingDataGenerated;
    private Boolean isDayEndProcessCompleted;
    private String userMessage;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
