package net.celloscope.mraims.loanportfolio.features.cancel.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelSamity {
    private String managementProcessId;
    private String samityEventTrackerId;
    private String officeId;
    private String samityId;
    private String samityEvent;
    private String remarks;
    private LocalDateTime createdOn;
    private String createdBy;
}
