package net.celloscope.mraims.loanportfolio.features.smsnotification.domain;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsLog {
    private String oid;
    private String mobileNo;
    private String sms;
    private String status;
    private String reason;
    private String queuedBy;
    private LocalDateTime queuedOn;
    private String smsSentBy;
    private LocalDateTime smsSentRequestOn;
    private LocalDateTime providerRequestOn;
    private LocalDateTime providerResponseOn;
    private String providerResponse;
    private Integer smsCount;
    private String providerOid;
    private Integer retryCount;
    private String langType;
    private String sortOrder;

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}

