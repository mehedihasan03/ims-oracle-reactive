package net.celloscope.mraims.loanportfolio.features.serviceCharge.adapter.out.persistence.database.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("template.service_charge_chart")
public class ServiceChargeChartEntity implements Persistable<String> {
    @Id
    private String oid;
    private String loanProductId;
    private String serviceChargeChartId;
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFreq;

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
