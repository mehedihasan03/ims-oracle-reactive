package net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.SMSNotificationMetaProperty;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustedLoanData;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanRebateAuthorizeCommand {
    private String managementProcessId;
    private String transactionProcessId;
    private String passbookProcessId;
    private String processId;
    private String mfiId;
    private String officeId;
    private String loginId;
    private String samityId;
    private String fieldOfficerId;
    private String memberId;
    private Integer limit;
    private Integer offset;
    private List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList;
    private Integer currentVersion;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
