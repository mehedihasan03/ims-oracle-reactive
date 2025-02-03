package net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.SMSNotificationMetaProperty;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanAdjustmentRequestDTO {

    private String id;
    private String managementProcessId;
    private String transactionProcessId;
    private String passbookProcessId;
    private String processId;
    private String mfiId;
    private String officeId;
    private String loginId;
    private String userRole;
    private String instituteOid;
    private String samityId;
    private String fieldOfficerId;
    private String memberId;
    private String adjustmentType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<AdjustedLoanData> data;
    private Integer limit;
    private Integer offset;
    private List<SMSNotificationMetaProperty> smsNotificationMetaPropertyList;
    private Integer currentVersion;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
