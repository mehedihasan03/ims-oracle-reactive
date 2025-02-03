package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.PaymentDetails;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawPaymentDataObject {

    private String stagingDataId;
    private String savingsAccountId;
    private BigDecimal amount;
    private String paymentMode;
    private PaymentDetails paymentDetails;

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
