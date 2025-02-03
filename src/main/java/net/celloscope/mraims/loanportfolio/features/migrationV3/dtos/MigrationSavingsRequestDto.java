package net.celloscope.mraims.loanportfolio.features.migrationV3.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class MigrationSavingsRequestDto {

    private String savingsProductName;
    private String savingsAccountId;
    private String companySavingsAccountId;
    private String savingsTypeId;
    private String shortNameDep;
    private BigDecimal interestRate;
    private LocalDate startDate;
    private  BigDecimal balance;
    private Integer depositTerm;
    private BigDecimal savingsAmount;
    private BigDecimal interest;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }

}
