package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ManagementProcessTrackerEntity {
    private String managementProcessId ;
    private String officeId;
    private String businessDay;
    private LocalDate businessDate;
}
