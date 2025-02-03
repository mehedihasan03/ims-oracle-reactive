package net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanProductDTO {
    private String loanProductId;
    private String loanProductNameEn;
    private String loanProductNameBn;
    private String loanProductDisplayName;
    private String mfiProgramId;
    private String productNature;
    private String loanTypeId;
    private BigDecimal minLoanAmount;
    private BigDecimal maxLoanAmount;
    private String repaymentFrequency;
    private Integer monthlyRepayDay;
    private String interestCalcMethod;
    private Integer defaultGraceDays;
    private String status;
    private String installmentInfoJson;


    /*private String oid;
    private String compLoanProductId;
    private String descProduct;
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
    private String migratedBy;
    private LocalDateTime migratedOn;
    private String createdBy;
    private LocalDateTime createdOn;
    private String updatedBy;
    private LocalDateTime updatedOn;
    private String closedBy;
    private LocalDateTime closedOn;
    private String mfiId;*/
}
