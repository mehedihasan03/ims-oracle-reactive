package net.celloscope.mraims.loanportfolio.features.dayforwardnew.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DayForwardProcessTracker {
    private String oid;
    private String managementProcessId;
    private String dayForwardProcessTrackerId;
    private String officeId;
    private String samityId;
    private String samityNameEn;
    private String samityNameBn;
    private String samityDay;
    private String status;
    private String archivingStatus;
    private String reschedulingStatus;
    private String remarks;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;
    private String createdBy;
    private LocalDateTime createdOn;
    private String retriedBy;
    private LocalDateTime retriedOn;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
