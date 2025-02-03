package net.celloscope.mraims.loanportfolio.features.stagingdata.domain.commands;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;
import java.time.LocalDate;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepayScheduleDTO extends BaseToString {

    private String loanRepayScheduleId;
    private Integer installNo;
    private LocalDate installDate;
    private BigDecimal totalPayment;
    private BigDecimal principal;
    private BigDecimal serviceCharge;
    private BigDecimal penalty;
    private BigDecimal fees;
    private BigDecimal insurance;
    private String status;

}
