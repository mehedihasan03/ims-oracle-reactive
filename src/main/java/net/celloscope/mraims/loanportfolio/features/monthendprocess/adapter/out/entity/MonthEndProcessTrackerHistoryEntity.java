package net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.out.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("month_end_process_tracker_history")
public class MonthEndProcessTrackerHistoryEntity implements Persistable<String> {

    @Id
    private String oid;
    private String managementProcessId;
    private String monthEndProcessTrackerId;
    private String officeId;
    private Integer month;
    private Integer year;
    private LocalDate monthEndDate;
    private String transactionCode;
    private String transactions;
    private BigDecimal totalAmount;
    private String aisRequest;
    private String aisResponse;
    private String status;
    private String remarks;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime retriedOn;
    private String retriedBy;
    private LocalDateTime archivedOn;
    private String archivedBy;

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
