package net.celloscope.mraims.loanportfolio.features.fdr.domain;

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
public class FDRClosureGridView {
    private String savingsAccountId;
    private String memberId;
    private String memberNameEn;
    private BigDecimal savingsAmount;
    private LocalDate acctStartDate;
    private LocalDate acctEndDate;
    private LocalDate acctCloseDate;
    private BigDecimal closingAmount;
    private String status;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
