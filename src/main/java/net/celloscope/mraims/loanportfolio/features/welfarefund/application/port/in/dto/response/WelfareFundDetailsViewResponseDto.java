package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelfareFundDetailsViewResponseDto {

    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String loanAccountId;
    private String loanProductId;
    private String loanProductNameEn;
    private String loanProductNameBn;
    private BigDecimal loanAmount;
    private BigDecimal serviceCharge;
    private BigDecimal totalLoanAmount;
    private String paymentMethod;
    private String referenceNo;
    private String transactionDate;
    private String amount;
    private String status;
    private String btnUpdateEnabled;
    private String btnAuthorizeEnabled;
    private String btnRejectEnabled;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
