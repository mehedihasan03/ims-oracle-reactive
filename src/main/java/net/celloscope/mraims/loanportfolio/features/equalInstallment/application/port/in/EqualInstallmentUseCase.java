package net.celloscope.mraims.loanportfolio.features.equalInstallment.application.port.in;

import net.celloscope.mraims.loanportfolio.features.equalInstallment.adapter.in.web.handler.dto.out.EqualInstallmentResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface EqualInstallmentUseCase extends MetaPropertyUseCase {
    Mono<EqualInstallmentResponseDTO> getEqualInstallmentAmount(Double loanAmount, Double serviceChargeRate, String serviceChargeRateFrequency, Integer noOfInstallments, String paymentPeriod, String daysInYear);

    Mono<EqualInstallmentResponseDTO> getEqualInstallmentAmountWhenPaymentPeriodUndefined(Double loanAmount, Double annualServiceChargeRate, Integer noOfInstallments, Integer loanTerm);
    Mono<EqualInstallmentResponseDTO> getEqualInstallmentAmountWhenPaymentPeriodUndefinedV2(BigDecimal loanAmount, BigDecimal annualServiceChargeRate, Integer noOfInstallments, Integer roundingToNextInteger, Integer loanTermInMonths);
    Mono<EqualInstallmentResponseDTO> getEqualInstallmentAmountAccordingToInterestCalcMethod(BigDecimal loanAmount, BigDecimal annualServiceChargeRate, Integer noOfInstallments, Integer loanTerm, String interestCalcMethod);
}
