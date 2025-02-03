package net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ServiceChargeChartResponseDTO {
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFreq;
    private String samityDay;
    private String repaymentFrequency;
    private String interestCalcMethod;
}
