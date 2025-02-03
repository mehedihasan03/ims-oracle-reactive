package net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.request;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.PaymentDetails;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WelfareFundRequestDto {

    private String mfiId;
    private String loginId;
    private String userRole;
    private String instituteOid;
    private String officeId;
    private String accountId;
    private String transactionDate;
    private Integer limit;
    private Integer offset;

    // for reset welfare fund data
    private String id;

    // for save collected data
    private String loanAccountId;
    private BigDecimal amount;
    private String paymentMethod;
    private String referenceNo;
    private PaymentDetails paymentDetails;
    private String samityId;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
