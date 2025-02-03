package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccruedInterestDTODomain {
    private String savingsAccountId;
    private String savingsAccountOid;
    private Integer interestCalculationMonth;
    private Integer interestCalculationYear;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer accruedDays;
    private BigDecimal accruedInterestAmount;
    private String productType;
    private BigDecimal savgAcctBeginBalance;
    private BigDecimal savgAcctEndingBalance;

    private String memberId;
    private String mfiId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }


}
