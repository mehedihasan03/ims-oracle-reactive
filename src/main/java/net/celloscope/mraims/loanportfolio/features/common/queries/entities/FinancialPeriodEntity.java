package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialPeriodEntity {

    private String oid;
    private String financialPeriodId;
    private String financialPeriodNameEn;
    private String financialPeriodNameBn;
    private String periodType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String officeId;
    private String mfiId;
    private String status;
    private String financialPeriodParentsId;
    private String weeklyHolidays;
    private String approvedBy;
    private LocalDate approvedOn;
    private String remarkedBy;
    private LocalDate remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private LocalDateTime createdOn;
    private String createdBy;
    private String updatedBy;
    private String updatedOn;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
