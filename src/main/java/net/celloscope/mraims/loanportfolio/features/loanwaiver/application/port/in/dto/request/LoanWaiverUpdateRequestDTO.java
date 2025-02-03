package net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedSavingsAccount;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanWaiverUpdateRequestDTO extends GenericLoanWaiverRequestDTO{

    private String id;
    private String loanAccountId;
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
