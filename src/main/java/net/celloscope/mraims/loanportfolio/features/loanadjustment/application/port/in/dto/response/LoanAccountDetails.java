package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

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
    private BigDecimal serviceCharge;
    private BigDecimal totalLoanAmount;

    private BigDecimal principalPaid;
    private BigDecimal serviceChargePaid;
    private BigDecimal totalPaid;

    private BigDecimal principalRemaining;
    private BigDecimal serviceChargeRemaining;
    private BigDecimal totalDue;
    private BigDecimal accountOutstanding;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
