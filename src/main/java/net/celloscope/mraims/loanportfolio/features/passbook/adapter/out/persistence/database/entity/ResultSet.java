package net.celloscope.mraims.loanportfolio.features.passbook.adapter.out.persistence.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table("passbook")
public class ResultSet implements Persistable<String> {
    @Id
    private String oid;
    private String loanRepayScheduleId;
    private Integer installNo;
    private LocalDate installDate;
    private BigDecimal installmentBeginBalance;
    private BigDecimal prinRemain;
    private BigDecimal scRemain;
    private BigDecimal installmentEndBalance;
    private BigDecimal totalLoanPaidTillDate;
    private BigDecimal totalLoanPayRemain;
    private BigDecimal prinPaidTillDate;
    private BigDecimal scPaidTillDate;

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
