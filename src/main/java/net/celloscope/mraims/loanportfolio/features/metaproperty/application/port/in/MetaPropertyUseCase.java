package net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in;

import net.celloscope.mraims.loanportfolio.features.dayendprocess.domain.AccountingMetaProperty;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.CalculationMetaProperty;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.MetaPropertyResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.EqualInstallmentMetaProperty;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.LoanCalculationMetaProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
public interface MetaPropertyUseCase {

    Mono<String> getEqualInstallmentPrecision();

    Mono<String> getEqualInstallmentServiceChargePrecision();

    Mono<String> getEqualInstallmentRoundingTo();

    Mono<String> getEqualInstallmentRoundingLogic();
    Mono<MetaPropertyResponseDTO> getMetaPropertyByPropertyId(String propertyId);
    Mono<CalculationMetaProperty> getCalculationMetaProperty();

    Mono<AccountingMetaProperty> getAccountingMetaProperty();
    Mono<LoanCalculationMetaProperty> getLoanCalculationMetaProperty(LocalDate businessDate);
    Mono<EqualInstallmentMetaProperty> getEqualInstallmentMetaProperty();

}
