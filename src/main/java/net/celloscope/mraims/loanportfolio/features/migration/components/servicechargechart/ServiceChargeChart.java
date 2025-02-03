package net.celloscope.mraims.loanportfolio.features.migration.components.servicechargechart;

import com.google.gson.GsonBuilder;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("service_charge_chart")
public class ServiceChargeChart {

    private String oid;
    private String serviceChargeChartId;
    private String loanProductId;
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFreq;
    private LocalDate scChartValidFrDate;
    private LocalDate scChartValidEndDate;
    private BigDecimal amountFrom;
    private BigDecimal amountTo;
    private String description;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String mfiId;
    private String status;
    private String migratedBy;
    private LocalDateTime migratedOn;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String closedBy;
    private LocalDateTime closedOn;

    @Override
    public String toString(){
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }
}
