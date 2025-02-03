package net.celloscope.mraims.loanportfolio.features.smsnotification.application.port.in.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotificationRequestDTO {
    private String type;
    private String id;
    private String amount;
    private String datetime;
    private String mfiId;
    private String instituteOid;
    private String loginId;
    private String accountId;
    private String memberId;
    private String mobileNumber;
    private String template;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}

