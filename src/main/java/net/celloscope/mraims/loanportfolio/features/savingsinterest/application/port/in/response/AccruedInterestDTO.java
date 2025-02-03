package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccruedInterestDTO {
    private String savingsAccountId;
    private String savingsAccountOid;
    private String savingsProductId;
    private String savingsTypeId;
    private Integer totalAccruedAccount;
    private BigDecimal totalInterestAccrued;
    private BigDecimal totalInterestPosted;
//    private YearMonth yearMonth;
    private Integer year;
    private Integer month;
    private String userMessage;

    // newly added
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
