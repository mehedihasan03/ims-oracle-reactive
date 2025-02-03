package net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceChargeInfoDTO {
    private String serviceChargeChartId;
    private String loanProductId;
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFreq;
    private LocalDate scChartValidFromDate;
    private LocalDate scChartValidEndDate;
    private BigDecimal amountFrom;
    private BigDecimal amountTo;
    private String description;
    private String status;

    /*private String oid;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String mfiId;
    private String migratedBy;
    private LocalDateTime migratedOn;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String closedBy;
    private LocalDateTime closedOn;*/

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
