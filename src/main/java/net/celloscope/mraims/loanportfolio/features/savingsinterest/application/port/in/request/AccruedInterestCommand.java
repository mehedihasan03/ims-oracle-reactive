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
public class AccruedInterestCommand {
    private String savingsAccountId;
    private BigDecimal interestRate;
    private LocalDate interestCalculationDate;
    private Integer interestCalculationMonth;
    private Integer interestCalculationYear;

    private String transactionId;
    private String managementProcessId;
    private String loginId;
    private String mfiId;

    private BigDecimal fdrInterest;

}
