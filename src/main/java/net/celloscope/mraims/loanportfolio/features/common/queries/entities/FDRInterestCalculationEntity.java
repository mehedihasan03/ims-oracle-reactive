package net.celloscope.mraims.loanportfolio.features.common.queries.entities;

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
public class FDRInterestCalculationEntity {

    private String savingsProductId;
    private String savingsAccountId;
    private BigDecimal savingsAmount;
    private Double interestRate;
    private String interestRateFrequency;
    private String interestPostingPeriod;
    private Integer depositTerm;
    private String depositTermPeriod;
    private LocalDate acctStartDate;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
