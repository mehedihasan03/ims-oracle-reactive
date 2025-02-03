package net.celloscope.mraims.loanportfolio.features.fdr.adapter.out.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("fdr_schedule")
public class FDRScheduleEntity implements Persistable<String> {
    @Id
    private String oid;
    private String savingsAccountId;
    private Integer postingNo;
    private LocalDate interestPostingDate;
    private BigDecimal calculatedInterest;
    private String status;
    private LocalDateTime createdOn;
    private String createdBy;

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
}
