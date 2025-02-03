package net.celloscope.mraims.loanportfolio.features.serviceCharge.application.port.in.helpers.dto;

import lombok.*;
import net.celloscope.mraims.loanportfolio.core.util.BaseToString;

import java.math.BigDecimal;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombinedDTO  extends BaseToString {
    private BigDecimal serviceChargeRate;
    private String serviceChargeRateFreq;
    private String samityDay;
    private String repaymentFrequency;
    private String interestCalcMethod;
}
