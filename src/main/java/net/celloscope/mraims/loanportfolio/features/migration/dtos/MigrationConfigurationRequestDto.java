package net.celloscope.mraims.loanportfolio.features.migration.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationConfigurationRequestDto {
    private String roundingMode;
    private String roundingInstallmentToNearestIntegerLogic;
    private Integer serviceChargeRatePrecision;
    private Integer principalAmountPrecision;
    private Integer installmentAmountPrecision;
    private String serviceChargeCalculationMethod;
    private LocalDate cutOffDate;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
