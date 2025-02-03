package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettleRebateRequestDto {
    private String mfiId;
    private String loginId;
    private String instituteOid;
    private String officeId;
    private String loanAccountId;
    private String id;
    private String memberId;
    private String samityId;
    private String rebatedAmount;
    private String payableAmount;
    private String collectionType;
    private String collectedAmountByCash;
    private List<RebateAdjustmentAccount> adjustedAccountList;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
