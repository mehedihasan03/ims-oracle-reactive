package net.celloscope.mraims.loanportfolio.features.archive.application.port.in.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataArchiveResponseDTO {

    private String managementProcessId;
    private String mfiId;
    private String officeId;
    private LocalDateTime archivedOn;
    private String archivedBy;
    private String userMessage;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
