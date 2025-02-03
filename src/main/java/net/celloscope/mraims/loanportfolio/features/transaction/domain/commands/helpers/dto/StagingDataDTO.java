package net.celloscope.mraims.loanportfolio.features.transaction.domain.commands.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StagingDataDTO {
    private String managementProcessId;
    private String processId;
    private String memberId;
    private String mfiId;
    private String stagingDataId;
    private LocalDate businessDate;
    private String samityId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }

}
