package net.celloscope.mraims.loanportfolio.features.dayendprocess.domain;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.response.helper.DayEndProcessProductTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DayEndProcessTracker {

    private String managementProcessId;
    private String dayEndProcessTrackerId;
    private String officeId;

    private String transactionCode;
    private List<DayEndProcessProductTransaction> transactions;
    private BigDecimal totalAmount;
    private String aisRequest;
    private String aisResponse;

    private String status;
    private String remarks;

    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;

    private String createdBy;
    private LocalDateTime createdOn;
    private String retriedBy;
    private LocalDateTime retriedOn;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
