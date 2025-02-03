package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.request;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelfareFundRequestBody {
    private String loanAccountId;
    private BigDecimal amount;
    private String paymentMethod;
    private String referenceNo;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
