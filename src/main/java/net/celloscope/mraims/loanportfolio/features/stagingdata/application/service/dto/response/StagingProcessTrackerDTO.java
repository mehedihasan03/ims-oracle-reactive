package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingProcessTrackerDTO extends BaseToString {

    private String processId;
    private String samityId;
    private List<String> stagingDataIds;
    private String status;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;
}
