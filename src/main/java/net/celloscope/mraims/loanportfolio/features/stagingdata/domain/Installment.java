package net.celloscope.mraims.loanportfolio.features.stagingdata.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Installment extends BaseToString {

    private String loanRepayScheduleId;
    private Integer installmentNo;
    private LocalDate installmentDate;
    private BigDecimal installmentAmount;
    private BigDecimal advance;
    private BigDecimal due;
    private BigDecimal penalty;
    private BigDecimal fees;
    private BigDecimal insurance;
    private String isCurrent;
}
