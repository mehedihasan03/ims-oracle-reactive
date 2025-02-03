package net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto;

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
public class InstallmentCalculationDTO {
    private String loanAccountId;
    private Integer installmentNo;
    private LocalDate installDate;
    private String loanRepayScheduleId;
    private BigDecimal prinPaid;
    private BigDecimal serviceChargePaid;
    private BigDecimal prinRemainForThisInst;
    private BigDecimal scRemainForThisInst;
    private BigDecimal prinPaidTillDate;
    private BigDecimal scPaidTillDate;
    private BigDecimal installmentBeginPrinBalance;
    private BigDecimal installmentEndPrinBalance;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
