package net.celloscope.mraims.loanportfolio.features.migration.interestchart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("interest_chart")
public class InterestChart {
    private String oid;
    private String interestChartId;
    private String savingsProductId;
    private BigDecimal interest;
    private LocalDate interestChartValidFrDate;
    private LocalDate interestChartValidEndDate;
    private BigDecimal balanceRangeFrom;
    private BigDecimal balanceRangeTo;
    private BigDecimal accountDurationFrom;
    private BigDecimal accountDurationTill;
    private String description;
    private String currentVersion;
    private String approvedBy;
    private LocalDate approvedOn;
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
    private String remarks;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String closedBy;
    private LocalDateTime closedOn;



    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }

}
