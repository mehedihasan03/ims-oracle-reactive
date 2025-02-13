package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanaccount;

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
@Table("template.loan_account")
public class LoanAccount {

    private String oid;
    private String loanApplicationId;
    private String loanAccountId;
    private String companyLoanAccountId;
    private String memberId;
    private String loanProductId;
    private BigDecimal origServiceChargeRate;
    private String productName;
    private String econPurposeMraCode;
    private String lendingCategoryId;
    private BigDecimal loanAmount;
    private Integer loanTerm;
    private Integer noInstallment;
    private String isGraceOverwritten;
    private Integer graceDays;
    private BigDecimal installmentAmount;
    private String scheduleCreationList;
    private BigDecimal noRescheduled;
    private String payMethod;
    private String insurance;
    private BigDecimal loanInsurancePremium;
    private BigDecimal loanInsurBorrowDeath;
    private LocalDate expectedDisburseDt;
    private LocalDate actualDisburseDt;
    private LocalDate plannedEndDate;
    private LocalDate actualEndDate;
    private String loanContractPhaseId;
    private String loanWriteOffId;
    private String fundSource;
    private String isSubsidizedCredit;
    private String isLawSuit;
    private String isWelfareFund;
    private String folioNo;
    private String guarantorListJson;
    private String currentVersion;
    private String isNewRecord;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String mfiId;
    private String loanClassStatus;
    private LocalDateTime loanClassStatusDate;
    private String status;
    private LocalDateTime loanAppliedOn;
    private String loanAppliedBy;
    private LocalDateTime disbursedOn;
    private String disbursedBy;
    private LocalDateTime closedOn;
    private String closedBy;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private BigDecimal serviceChargeRatePerPeriod;

    private String microEnName;
    private String microLegalForm;

    private LocalDate businessDate;
    private String managementProcessId;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
