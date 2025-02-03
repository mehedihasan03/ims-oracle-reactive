package net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.out.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("day_forward_process_tracker_history")
public class DayForwardProcessTrackerHistoryEntity implements Persistable<String> {
    @Id
    private String oid;
    private String managementProcessId;
    private String dayForwardProcessTrackerId;
    private String officeId;
    private String samityId;

    private String status;
    private String archivingStatus;
    private String reschedulingStatus;
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
        return this.getOid();
    }

    @Override
    public boolean isNew() {
        boolean isNull = Objects.isNull(this.oid);
        this.oid = isNull ? UUID.randomUUID().toString() : this.oid;
        return isNull;
    }

    @Override
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
