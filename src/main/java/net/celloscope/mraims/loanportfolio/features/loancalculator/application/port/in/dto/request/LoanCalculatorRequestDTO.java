package net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanCalculatorRequestDTO {
    private String loanProductId;
    private BigDecimal loanAmount;
    private Integer noOfInstallments;
    private Integer graceDays;
    private LocalDate disbursementDate;
    private String samityDay;
    private String officeId;

    // Optional fields
    private Integer roundingToNearestInteger;
    private BigDecimal installmentAmount;
    private Integer loanTermInMonths;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
