package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;
import net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response.StagingDataGenerationStatusDTO;

import java.util.List;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataGenerationResponseDTO extends BaseToString {

    private List<StagingDataGenerationStatusDTO> stagingDataGenerationStatus;
    private Long totalCount;
}
