package net.celloscope.mraims.loanportfolio.features.interestcompound.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InterestCompoundDTO {
    private String savingsAccountId;
    private String compoundBalance;
    private String compoundingDate;
    private String compoundBalanceBeforeInstallment;
    private String compoundBalanceAfterInstallment;
    private String accruedInterestBeforeInstallment;
    private String accruedInterestAfterInstallment;
    private String managementProcessId;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
