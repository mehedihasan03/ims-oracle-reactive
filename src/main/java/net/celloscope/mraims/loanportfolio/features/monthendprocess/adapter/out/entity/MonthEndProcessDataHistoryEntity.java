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
import java.time.LocalDateTime;
import java.util.UUID;

import static java.util.Objects.isNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("template.month_end_process_data_history")
public class MonthEndProcessDataHistoryEntity implements Persistable<String> {
    @Id
    private String oid;
    private String managementProcessId;
    private String monthEndProcessTrackerId;
    private String officeId;
    private String samityId;

    private BigDecimal totalAccruedAmount;
    private BigDecimal totalPostingAmount;

    private String status;
    private String remarks;
    private LocalDateTime processStartTime;
    private LocalDateTime processEndTime;

    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime retriedOn;
    private String retriedBy;
    private String archivedBy;
    private LocalDateTime archivedOn;

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
    public String toString(){
        return CommonFunctions.buildGsonBuilder(this);
    }
}
