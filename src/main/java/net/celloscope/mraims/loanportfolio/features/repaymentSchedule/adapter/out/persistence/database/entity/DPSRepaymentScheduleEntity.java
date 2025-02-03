package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity;

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
@AllArgsConstructor
@NoArgsConstructor
@Table("dps_repayment_schedule")
public class DPSRepaymentScheduleEntity implements Persistable<String> {
    @Id
    private String oid;
    private String dpsRepaymentScheduleId;
    private String savingsAccountId;
    private String savingsAccountOid;
    private String memberId;
    private String samityId;
    private Integer repaymentNo;
    private LocalDate repaymentDate;
    private String dayOfWeek;
    private BigDecimal repaymentAmount;
    private String mfiId;
    private LocalDateTime createdOn;
    private String createdBy;
    private LocalDateTime updatedOn;
    private String updatedBy;
    private String status;
    private LocalDate actualRepaymentDate;
    private String managementProcessId;

    @Override
    public String getId() {
        return this.oid;
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
