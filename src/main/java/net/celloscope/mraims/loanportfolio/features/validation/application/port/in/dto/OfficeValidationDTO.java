package net.celloscope.mraims.loanportfolio.features.validation.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.processmanagement.domain.ManagementProcessTracker;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeValidationDTO {

    private String managementProcessId;
    private ManagementProcessTracker managementProcessTracker;
    private String mfiId;
    private String officeId;
    private LocalDate businessDate;
    private String businessDay;
    private List<String> officeEvents;
    private List<String> samityIdList;
    private List<SamityValidationDTO> samityList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
