package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DpsRepaymentDTO {
    private String savingsAccountId;
    private String savingsAccountOid;
    private String memberId;
    private String samityId;
    private Integer repaymentNo;
    private LocalDate repaymentDate;
    private String dayOfWeek;
    private BigDecimal repaymentAmount;
    private String status;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
