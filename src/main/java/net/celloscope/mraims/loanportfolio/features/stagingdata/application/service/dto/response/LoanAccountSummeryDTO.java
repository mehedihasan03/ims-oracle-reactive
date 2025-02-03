package net.celloscope.mraims.loanportfolio.features.stagingdata.application.service.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountSummeryDTO extends BaseToString {

    private String productCode;
    private String productNameEn;
    private String productNameBn;
    private BigDecimal totalDue;
}
