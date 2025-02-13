package net.celloscope.mraims.loanportfolio.features.migration.components.savingsaccount;

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
@Table("template.savings_account")
public class SavingsAccount {
    private String oid;
    private String savingsApplicationId;
    private String savingsAccountId;
    private String savingsProductId;
    private String savingsTypeId;
    private String savingsProdNameEn;
    private String memberId;
    private String mfiProgramId;
    private String interestRateTerms;
    private String interestRateFrequency;
    private String interestCalculatedUsing;
    private String interestPostingPeriod;
    private String interestCompoundingPeriod;
    private BigDecimal minOpeningBalance;
    private String lockInPeriod;
    private String lockInPeriodFrequency;
    private BigDecimal balanceRequiredInterestCalc;
    private Integer enforceMinimumBalance;
    private Integer depositTerm;
    private String depositTermPeriod;
    private String depositEvery;
    private BigDecimal minBalance;
    private BigDecimal minDepositAmount;
    private BigDecimal maxDepositAmount;
    private BigDecimal minInstallmentAmount;
    private BigDecimal maxInstallmentAmount;
    private String enableDormancyTracking;
    private String daysToInactiveSubStatus;
    private String daysToDormantSubStatus;
    private BigDecimal fees;
    private LocalDate acctStartDate;
    private LocalDate acctEndDate;
    private BigDecimal vsInstallment;
    private String applyWithdrawFeeTransfers;
    private String allowWithdrawals;
    private String monthlyRepayDay;
    private String interestPostingDates;
    private String mfiId;
    private LocalDateTime loanAppliedOn;
    private String loanAppliedBy;
    private String currentVersion;
    private String isNewRecord;
    private String lockedBy;
    private LocalDateTime lockedOn;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private LocalDateTime rejectedOn;
    private String rejectedBy;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String status;
    private BigDecimal balance;
    private String scPostingPeriod;
    private BigDecimal gsInstallment;
    private Integer accountTerm;
    private BigDecimal savingsAmount;
    private BigDecimal maturityAmount;
    private BigDecimal prematureClosePenalInter;
    private String recurringDepositType;


    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
