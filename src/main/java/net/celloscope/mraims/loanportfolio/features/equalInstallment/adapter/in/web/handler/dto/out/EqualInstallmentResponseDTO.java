package net.celloscope.mraims.loanportfolio.features.equalInstallment.adapter.in.web.handler.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EqualInstallmentResponseDTO {
    /*String message;*/
    BigDecimal equalInstallmentAmount;
    BigDecimal serviceChargeRatePerPeriod;
    Integer roundingToNextInteger;
    BigDecimal minimumEqualInstallmentAmount;
    BigDecimal maximumEqualInstallmentAmount;
}
