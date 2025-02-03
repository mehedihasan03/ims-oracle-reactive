package net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEndProcessRequestDTO {

    private String mfiId;
    private String loginId;
    private String officeId;
    private Boolean isScheduledRequest;
    private List<String> transactionCodeList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
