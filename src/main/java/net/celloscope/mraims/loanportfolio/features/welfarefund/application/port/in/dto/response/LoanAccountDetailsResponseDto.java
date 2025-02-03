package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.response;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAccountDetailsResponseDto {
    private String memberId;
    private String memberNameEn;
    private String memberNameBn;
    private String loanAccountId;
    private String loanProductId;
    private String loanProductNameEn;
    private String loanProductNameBn;
    private String loanAmount;
    private String serviceCharge;
    private String totalLoanAmount;
    private String status;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
