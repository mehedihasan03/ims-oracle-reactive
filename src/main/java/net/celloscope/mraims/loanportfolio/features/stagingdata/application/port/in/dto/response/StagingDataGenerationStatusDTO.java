package net.celloscope.mraims.loanportfolio.features.stagingdata.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingDataGenerationStatusDTO {

    private String managementProcessId;
    private String processId;
    private String officeId;
    private LocalDate businessDate;
    private String businessDay;

    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private Integer totalMember;

    private String fieldOfficerId;
    private String fieldOfficerNameEn;
    private String fieldOfficerNameBn;

    private String status;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
