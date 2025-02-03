package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response;

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
public class SavingsAccruedInterestResponseDTO {
    private String memberId;
    private String mfiId;
    private String accruedInterestId;
    private String savingsAccountId;
    private String savingsAccountOid;
    private Integer interestCalculationMonth;
    private Integer interestCalculationYear;
    private BigDecimal accruedInterestAmount;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Integer accruedDays;
    private BigDecimal savgAcctBeginBalance;
    private BigDecimal savgAcctEndingBalance;

    private String managementProcessId;
    private String savingsTypeId;
    private String savingsProductId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
