package net.celloscope.mraims.loanportfolio.features.processmanagement.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.accounting.adapter.out.web.dto.AisResponse;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayEndProcessResponseDTO {

    private String managementProcessId;
    private String mfiId;
    private String officeId;
    private LocalDate businessDate;
    private String businessDay;
    private String dayEndProcessRunBy;
    private String userMessage;
    private AisResponse aisResponse;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
