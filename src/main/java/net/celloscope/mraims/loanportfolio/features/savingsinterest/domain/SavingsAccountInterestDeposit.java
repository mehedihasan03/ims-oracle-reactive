package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingsAccountInterestDeposit {
    private String oid;
    private String accruedInterestId;
    private String savingsAccountId;
    private String savingsAccountOid;
    private String managementProcessId;
    private String processId;
    private String memberId;
    private String officeId;
    private String samityId;
    private String transactionId;
    private String productId;
    private String savingsTypeId;
    private Integer interestCalculationMonth;
    private Integer interestCalculationYear;
    private BigDecimal accruedInterestAmount;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BigDecimal savgAcctBeginBalance;
    private BigDecimal savgAcctEndingBalance;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;
    private String remarks;
}
