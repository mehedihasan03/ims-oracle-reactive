package net.celloscope.mraims.loanportfolio.features.migration.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MigrationDPSRepaymentScheduleCommand {
    private String savingsAccountId;
    private LocalDate cutOffDate;
    private Integer noOfPaidInstallments;
    private Integer monthlyRepaymentFrequencyDay;
    private String loginId;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}