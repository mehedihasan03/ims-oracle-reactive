package net.celloscope.mraims.loanportfolio.features.migrationV3.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class MigrationLoanRequestDto {

    private String loanProductName;
    private String loanAccountId;
    private String companyLoanAccountId;
    private String mfiProgramId;
    private String lendingCategoryId;
    private String repaymentFrequency;
    private BigDecimal monthlyRepayDay;
    private String loanTypeId;
    private Integer noInstallment;
    private Integer loanTerm;
    private Integer defaultGraceDays;
    private String isGraceOverwritten;
    private LocalDate loanDisbursementDate;
    private BigDecimal disbursedLoanAmount;
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFreq;
    private BigDecimal loanOutstanding;
    private BigDecimal overDueAmount;
    private String econPurposeMraCode;
    //v3
    private LocalDate lastInstallmentPaymentDate;
    private BigDecimal totalServiceCharge;
    private BigDecimal principleOutstanding;
    private BigDecimal serviceChargeOutstanding;
    private BigDecimal totalOutstandingAmount;
    private Integer overDueInstallment;
    private Integer remainingInstallment;
    private BigDecimal installmentPrincipleAmount;
    private BigDecimal installmentServiceCharge;
    private String loanStatus;
    private Integer noOfTimeRescheduling;
    private LocalDate dateOfLastReschedule;
    private BigDecimal writeOffAmount;
    private LocalDate writeOffDate;
    private String contractPhase;
    private String flagSubsidizedCredit;
    private String modeOfPayment;
    private String lawSuit;
    private String ME;
    private String memberWelfareFundCoverage;
    private String insuranceCoverage;
    private Integer loanCycle;


    private BigDecimal installmentAmount;


    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
