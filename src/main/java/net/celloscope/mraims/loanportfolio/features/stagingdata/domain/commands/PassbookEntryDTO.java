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
public class PassbookEntryDTO extends BaseToString {


    private String loanAccountId;
    private Integer installNo;
    private LocalDate installDate;
    private BigDecimal prinPaid;
    private BigDecimal prinPaidTillDate;
    private BigDecimal prinRemainForThisInst;
    private BigDecimal serviceChargePaid;
    private BigDecimal scPaidTillDate;
    private BigDecimal scRemainForThisInst;
    private String status;

}
