package net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.fdr.domain.FDRSchedule;

import java.util.List;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FDRResponseDTO {
    private List<FDRSchedule> fdrScheduleInterestPostingList;
    private String message;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
