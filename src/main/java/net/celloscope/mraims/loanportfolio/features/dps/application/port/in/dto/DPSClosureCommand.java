package net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto;

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
public class DPSClosureCommand {
    private String savingsAccountId;
    private LocalDate closingDate;
    private String paymentMode;
    private BigDecimal effectiveInterestRate;
    private String referenceAccountId;
    private String loginId;
    private String officeId;

    private BigDecimal maturityAmount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
