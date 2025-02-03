package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RepaymentScheduleResponseDTO {

    private String loanRepayScheduleId;
    private String loanAccountId;
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private Integer installNo;
    private LocalDate installDate;
    private BigDecimal beginPrinBalance;
    private BigDecimal totalPayment;
    private BigDecimal principal;
    private BigDecimal serviceCharge;
    private BigDecimal endPrinBalance;
    private BigDecimal scheduledPayment;
    private BigDecimal extraPayment;

    private BigDecimal penalty;
    private BigDecimal fees;
    private BigDecimal insurance;
    private String status;

    private BigDecimal serviceChargeRatePerPeriod;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
