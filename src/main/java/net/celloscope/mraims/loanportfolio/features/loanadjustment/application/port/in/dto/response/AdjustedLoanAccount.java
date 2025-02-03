package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustedLoanAccount {

    private String oid;
    private String loanAccountId;
    private BigDecimal loanAmount;
    private BigDecimal totalPrincipalRemaining;
    private BigDecimal totalServiceChargeRemaining;
    private BigDecimal accountOutstanding;
    private BigDecimal totalLoanAmount;

    private LocalDate adjustmentDate;
    private BigDecimal adjustedAmount;
    private BigDecimal totalDue;
    private String status;
    private List<AdjustedSavingsAccount> adjustedSavingsAccountList;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
