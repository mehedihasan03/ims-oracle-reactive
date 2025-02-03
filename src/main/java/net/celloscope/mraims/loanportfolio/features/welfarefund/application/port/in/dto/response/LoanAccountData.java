package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountData {

    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String loanAccountId;
    private BigDecimal loanAmount;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String status;
    private String btnUpdateEnabled;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
