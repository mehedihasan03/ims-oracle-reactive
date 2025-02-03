package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelfareFundLoanAccountData {

    private BigDecimal amount;
    private String status;
    private LocalDate transactionDate;
    private String paymentMethod;
    private String referenceNo;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
