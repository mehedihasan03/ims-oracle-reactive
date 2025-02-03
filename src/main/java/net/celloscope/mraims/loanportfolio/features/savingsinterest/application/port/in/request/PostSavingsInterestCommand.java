package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request;

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
public class PostSavingsInterestCommand {
    private String savingsAccountId;
    private BigDecimal interestAmount;
    private LocalDate interestPostingDate;
    private String loginId;
    private String officeId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
