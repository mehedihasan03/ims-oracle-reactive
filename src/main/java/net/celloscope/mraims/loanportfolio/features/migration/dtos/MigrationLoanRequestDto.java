package net.celloscope.mraims.loanportfolio.features.migration.dtos;

import com.google.gson.GsonBuilder;
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

    private BigDecimal installmentAmount;


    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
