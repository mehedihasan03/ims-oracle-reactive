package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RebateInfoEntity extends BaseToString {
    private BigDecimal totalPayable;
    private BigDecimal totalPrincipal;
    private BigDecimal totalServiceCharge;
    /*private BigDecimal totalOutstanding;
    private BigDecimal totalRemainingPrincipal;
    private BigDecimal totalRemainingServiceCharge;
    private BigDecimal totalRebateable;*/
}
