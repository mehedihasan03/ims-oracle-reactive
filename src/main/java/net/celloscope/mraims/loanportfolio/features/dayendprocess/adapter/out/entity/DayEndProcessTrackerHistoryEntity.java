package net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.out.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "template.day_end_process_tracker_history")
public class DayEndProcessTrackerHistoryEntity implements Persistable<String> {

    @Id
    private String oid;
    private String managementProcessId;
    private String dayEndProcessTrackerId;
    private String officeId;
    private String transactionCode;
    private String transactions;
    private BigDecimal totalAmount;
    private String aisRequest;
    private String aisResponse;
    private String status;
    private String remarks;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;
    private String createdBy;
    private LocalDateTime createdOn;
    private String retriedBy;
    private LocalDateTime retriedOn;
    private String archivedBy;
    private LocalDateTime archivedOn;

    @Override
    public String getId() {
        return this.oid;
    }

    public void setId(String id) {
        this.oid = id;
    }

    @Override
    public boolean isNew() {
        boolean isNull = Objects.isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }

    @Override
    public String toString() {
        return CommonFunctions.buildGsonBuilder(this);
    }
}
