package net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in.dto;

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
public class SavingsClosureCommand {

    private String savingsAccountId;
    private LocalDate closingDate;
    private String paymentMode;
    private String loginId;
    private String officeId;
    private String mfiId;
    private String remarks;

    private BigDecimal closingAmount;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
