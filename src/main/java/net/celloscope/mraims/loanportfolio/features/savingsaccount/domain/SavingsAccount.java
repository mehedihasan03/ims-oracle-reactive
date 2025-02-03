package net.celloscope.mraims.loanportfolio.features.savingsaccount.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsAccount {
    
    private String savingsApplicationId;
    private String savingsAccountId;
    private String savingsProductId;
    private String savingsTypeId;
    private String savingsProductNameEn;
    private String savingsProductNameBn;
    private String memberId;
    private String scPostingPeriod;
    private BigDecimal minOpeningBalance;
    private BigDecimal minBalance;
    private BigDecimal gsInstallment;
    private String lockInPeriod;
    private BigDecimal balanceRequiredInterestCalc;
    private BigDecimal savingsAmount;
    private Integer accountTerm;
    private BigDecimal prematureClosePenalInter;
    private String recurringDepositType;
    private String depositEvery;
    private BigDecimal fees;
    private LocalDate acctStartDate;
    private LocalDate acctEndDate;
    private BigDecimal vsInstallment;
    private String applyWithdrawFeeTransfers;
    private String allowWithdrawals;
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
    private String status;
    private BigDecimal balance;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
