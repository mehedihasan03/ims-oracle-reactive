package net.celloscope.mraims.loanportfolio.features.serviceCharge.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceChargeChart {
    private String oid;
    private String loanProductId;
    private String serviceChargeChartId;
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFreq;
}
