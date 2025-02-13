package net.celloscope.mraims.loanportfolio.features.migration.components.savingsproduct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("template.savings_product")
public class SavingsProduct {
    private String oid;
    private String savingsProductId;
    private String savingsTypeId;
    private String shortNameDep;
    private String companySavingsProductId;
    private String mfiProgramId;
    private String savingsProdNameEn;
    private String savingsProdNameBn;
    private String displayName;
    private String descSavingsProd;
    private String interestRateTerms;
    private String interestRateFrequency;
    private String interestCalculatedUsing;
    private String interestPostingPeriod;
    private String interestCompoundingPeriod;
    private BigDecimal minOpeningBalance;
    private String lockInPeriod;
    private String lockInPeriodFrequency;
    private BigDecimal balanceRequiredInterestCalc;
    private BigDecimal enforceMinimumBalance;
    private String depositTerm;
    private String depositTermPeriod;
    private String depositEvery;
    private BigDecimal minBalance;
    private BigDecimal minDepositAmount;
    private BigDecimal maxDepositAmount;
    private BigDecimal minInstallmentAmount;
    private BigDecimal maxInstallmentAmount;
    private String monthlyRepayDay;
    private String mfiId;
    private String status;
    private String enableDormancyTracking;
    private String daysToInactiveSubStatus;
    private String daysToDormantSubStatus;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String isApproverRemarks;
    private String approverRemarks;
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
