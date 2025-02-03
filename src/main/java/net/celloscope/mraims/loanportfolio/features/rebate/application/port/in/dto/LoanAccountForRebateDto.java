package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanAccountForRebateDto {
    private String loanAccountId;
    private String loanProductId;
    private String loanProductNameEn;
    private String loanProductNameBn;
    private BigDecimal loanAmount;
    private BigDecimal serviceCharge;
    private BigDecimal totalLoanAmount;
    private BigDecimal principalPaid;
    private BigDecimal serviceChargePaid;
    private BigDecimal totalPaid;
    private BigDecimal principalRemaining;
    private BigDecimal serviceChargeRemaining;
    private BigDecimal totalDue;
    private BigDecimal rebatableAmount;
    private String disbursementDate;
    private int loanTerm;
    private BigDecimal installmentAmount;
    private int noOfInstallment;
    private BigDecimal payableAmount;
    private BigDecimal serviceChargeRate;
    private BigDecimal advancePaid;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
