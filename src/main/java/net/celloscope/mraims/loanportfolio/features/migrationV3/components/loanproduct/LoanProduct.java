package net.celloscope.mraims.loanportfolio.features.migrationV3.components.loanproduct;

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
@Table("loan_product")
public class LoanProduct {

    private String oid;
    private String loanProductId;
    private String compLoanProductId;
    private String loanProductNameEn;
    private String loanProductNameBn;
    private String loanProductDisplayName;
    private String descProduct;
    private String mfiProgramId;
    private String productNature;
    private String loanTypeId;
    private BigDecimal minLoanAmount;
    private BigDecimal maxLoanAmount;
    private String repaymentFrequency;
    private BigDecimal monthlyRepayDay;
    private String interestCalcMethod;
    private Integer defaultGraceDays;
    private String fundSource;
    private LocalDate loanProductStartDate;
    private LocalDate loanProductCloseDate;
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
        return CommonFunctions.buildGsonBuilder(this);
    }
}
