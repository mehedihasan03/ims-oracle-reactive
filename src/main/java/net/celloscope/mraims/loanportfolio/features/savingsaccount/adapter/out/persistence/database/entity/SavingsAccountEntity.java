package net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter.out.persistence.database.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.savings_account")
public class SavingsAccountEntity implements Persistable<String> {

    @Id
    private String oid;
    
    private String savingsApplicationId;
    private String savingsAccountId;
    private String savingsProductId;
    private String savingsTypeId;
    private String savingsProdNameEn;
    private BigDecimal minOpeningBalance;
    private BigDecimal minBalance;
    private BigDecimal gsInstallment;
    private LocalDate acctStartDate;
    private LocalDate acctEndDate;
    private String mfiId;
    private String status;
    private String memberId;
    private String scPostingPeriod;
    private String lockInPeriod;
    private BigDecimal balanceRequiredInterestCalc;
    private BigDecimal savingsAmount;
    private Integer accountTerm;
    private BigDecimal prematureClosePenalInter;
    private String recurringDepositType;
    private String depositEvery;
    private BigDecimal fees;
    private BigDecimal vsInstallment;
    private String applyWithdrawFeeTransfers;
    private String allowWithdrawals;
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

    private Integer depositTerm;
    private String depositTermPeriod;

    // from SavingsProduct
    private BigDecimal minDepositAmount;
    private BigDecimal maxDepositAmount;

    private LocalDateTime updatedOn;
    private String updatedBy;

    private BigDecimal maturityAmount;
    private String interestPostingDates;
    private BigDecimal balance;
    private BigDecimal openingBalance;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }


    @Override
    public String getId() {
        return this.getOid();
    }

    @Override
    public boolean isNew() {
        boolean isNull = isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }
}
