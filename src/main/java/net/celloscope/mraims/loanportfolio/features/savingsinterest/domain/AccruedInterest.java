package net.celloscope.mraims.loanportfolio.features.savingsinterest.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccruedInterest {
    private String accruedInterestId;
    private String savingsAccountId;
    private String savingsAccountOid;
    private String managementProcessId;
    private String memberId;
    private String officeId;
    private String transactionId;
    private String productId;
    private String savingsTypeId;
    private Integer interestCalculationMonth;
    private Integer interestCalculationYear;
    private BigDecimal accruedInterestAmount;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;
    private String remarks;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
