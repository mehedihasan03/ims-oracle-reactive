package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataGenerationStatusResponseDTO extends BaseToString {

    private String isEnabled;
    private String userMessage;
    private List<StagingDataGenerationStatusDTO> stagingDataGenerationStatus;
    private Long totalCount;
}
