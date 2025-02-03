package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountDetails {

    private String loanAccountId;
    private String loanProductId;
    private String loanProductNameEn;
    private String loanProductNameBn;

    private BigDecimal loanAmount;
    private BigDecimal totalLoanAmount;


    private BigDecimal serviceCharge;
    private BigDecimal serviceChargePaid;
    private BigDecimal serviceChargeRemaining;

    private BigDecimal principalPaid;
    private BigDecimal principalRemaining;

    private BigDecimal totalPaid;

    private BigDecimal totalDue;

    private BigDecimal advancePaid;

    private LocalDate disbursementDate;

    private int loanTerm;
    private BigDecimal installmentAmount;
    private int noOfInstallment;
    private BigDecimal serviceChargeRate;


    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
