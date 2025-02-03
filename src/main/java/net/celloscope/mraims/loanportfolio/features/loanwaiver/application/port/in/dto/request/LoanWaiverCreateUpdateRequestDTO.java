package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedSavingsAccount;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWaiverCreateUpdateRequestDTO extends GenericLoanWaiverRequestDTO{

    private String managementProcessId;
    private String processId;

    private String id;
    private String loanAccountId;
    private String samityId;
    private String memberId;
    private BigDecimal waivedAmount;
    private BigDecimal payableAmount;
    private String collectionType;
    private BigDecimal collectedAmountByCash;
    private List<AdjustedSavingsAccount> adjustedAccountList;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
