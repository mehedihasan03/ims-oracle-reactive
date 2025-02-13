package net.celloscope.mraims.loanportfolio.features.smsnotification.adapter.out.persistence.database.entity;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.sms_log")
public class SmsLogEntity implements Persistable<String> {
    @Id
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
    public String getId() {
        return this.getOid();
    }

    @Override
    public boolean isNew() {
        boolean isNull = isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}

