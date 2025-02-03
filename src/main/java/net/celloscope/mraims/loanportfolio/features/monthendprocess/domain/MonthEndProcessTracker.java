package net.celloscope.mraims.loanportfolio.features.monthendprocess.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthEndProcessTracker {

    private String managementProcessId;
    private String monthEndProcessTrackerId;
    private String officeId;
    private Integer month;
    private Integer year;
    private LocalDate monthEndDate;

    private String transactionCode;
    private List<MonthEndProcessProductTransaction> transactions;
    private BigDecimal totalAmount;

    private String aisRequest;
    private String aisResponse;

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
