package net.celloscope.mraims.loanportfolio.features.fdr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FDREncashment {
    private String savingsAccountId;
    private BigDecimal savingsAmount;
    private BigDecimal effectiveInterestRate;
    private BigDecimal interest;
    private BigDecimal maturityAmount;
    private LocalDate acctStartDate;
    private LocalDate acctEndDate;
    private String paymentMode;
    private String referenceGSAccountId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
