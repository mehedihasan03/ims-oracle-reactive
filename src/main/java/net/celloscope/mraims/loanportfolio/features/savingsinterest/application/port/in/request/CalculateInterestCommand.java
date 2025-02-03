package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CalculateInterestCommand {
    private String savingsAccountId;
    private LocalDate interestCalculationDate;
    private Integer interestCalculationMonth;
    private Integer interestCalculationYear;
    private LocalDate businessDate;

//    private BigDecimal interestRate;
/*    private String transactionId;
    private String managementProcessId;
    private String loginId;
    private String mfiId;*/

}
