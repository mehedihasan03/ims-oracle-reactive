package net.celloscope.mraims.loanportfolio.features.archive.application.port.in.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataArchiveRequestDTO {

    private String managementProcessId;
    private String mfiId;
    private String loginId;
    private String officeId;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
