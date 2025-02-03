package net.celloscope.mraims.loanportfolio.features.monthendprocess.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthEndProcessData {

    private String managementProcessId;
    private String monthEndProcessTrackerId;
    private String officeId;
    private String samityId;

    private BigDecimal totalAccruedAmount;
    private BigDecimal totalPostingAmount;

    private String status;
    private String remarks;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;

    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime retriedOn;
    private String retriedBy;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
