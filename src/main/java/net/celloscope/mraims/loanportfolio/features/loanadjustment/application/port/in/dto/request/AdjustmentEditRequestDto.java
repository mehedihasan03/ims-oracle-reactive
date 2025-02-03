package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request;

import lombok.*;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.response.AdjustedSavingsAccount;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjustmentEditRequestDto {
    private String id;
    private String loginId;
    private String mfiId;
    private String instituteOid;
    private String userRole;
    private String oid;
    private String loanAccountId;
    private BigDecimal adjustedAmount;
    private List<AdjustedSavingsAccount> adjustedSavingsAccountList;
}
