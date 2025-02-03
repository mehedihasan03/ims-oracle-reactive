package net.celloscope.mraims.loanportfolio.features.migration.components.loanapplication;

import com.google.gson.GsonBuilder;
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
@Table("loan_application")
public class LoanApplication {
    private String oid;
    private String loanApplicationId;
    private String memberId;
    private String loanProductId;
    private String econPurposeMraCode;
    private String lendingCategoryId;
    private BigDecimal appliedLoanAmount;
    private BigDecimal approvedLoanAmount;
    private Integer loanTerm;
    private Integer noInstallment;
    private Integer graceDays;
    private String isGraceOverwritten;
    private BigDecimal installmentAmount;
    private String payMethodId;
    private String insurance;
    private BigDecimal loanInsurancePremium;
    private BigDecimal loanInsurBorrowDeath;
    private LocalDate expectedDisburseDt;
    private String loanContractPhaseId;
    private String fundSource;
    private String isSubsidizedCredit;
    private String isWelfareFund;
    private String folioNo;
    private String guarantorListJson;
    private String mfiId;
    private LocalDateTime loanAppliedOn;
    private String loanAppliedBy;
    private String verifiedBy;
    private LocalDateTime verifiedOn;
    private LocalDateTime rejectedOn;
    private String rejectedBy;
    private String status;
    private String currentVersion;
    private String isNewRecord;
    private String lockedBy;
    private LocalDateTime lockedOn;
    private String approvedBy;
    private LocalDateTime approvedOn;
    private String remarkedBy;
    private LocalDateTime remarkedOn;
    private String isApproverRemarks;
    private String approverRemarks;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;

    private String microEnName;
    private String microLegalForm;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
