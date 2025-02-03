package net.celloscope.mraims.loanportfolio.features.metaproperty.application.service;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.domain.AccountingMetaProperty;
import net.celloscope.mraims.loanportfolio.features.metaproperty.adapter.out.persistence.entity.MetaPropertyEntity;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.CalculationMetaProperty;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.MetaPropertyResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.out.MetaPropertyPersistencePort;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.*;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static net.celloscope.mraims.loanportfolio.core.util.enums.Status.STATUS_ACTIVE;
import static net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyParamEnum.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Primary
public class MetaPropertyService implements MetaPropertyUseCase {

    public final MetaPropertyPersistencePort port;

    @Override
    public Mono<String> getEqualInstallmentPrecision() {
        return port.getEqualInstallmentParams()
                .switchIfEmpty(Mono.just(
                        MetaProperty
                        .builder()
                        .params(List.of(
                                MetaPropertyParam
                                .builder()
                                .name(INSTALLMENT_PRECISION.getValue())
                                .value("2")
                                .build()))
                        .build()))
                .map(metaProperty -> metaProperty.getParams().stream()
                        .filter(metaPropertyParam -> metaPropertyParam.getName().equals(INSTALLMENT_PRECISION.getValue()))
                        .map(MetaPropertyParam::getValue)
                        .toList().get(0));
    }

    @Override
    public Mono<String> getEqualInstallmentServiceChargePrecision() {
        return port.getEqualInstallmentParams()
                .switchIfEmpty(Mono.just(
                        MetaProperty
                        .builder()
                        .params(List.of(
                                MetaPropertyParam
                                .builder()
                                .name(SERVICE_CHARGE_PRECISION.getValue())
                                .value("8")
                                .build()))
                        .build()))
                .map(metaProperty -> metaProperty.getParams().stream()
                        .filter(metaPropertyParam -> metaPropertyParam.getName().equals(SERVICE_CHARGE_PRECISION.getValue()))
                        .map(MetaPropertyParam::getValue)
                        .toList().get(0));
    }

    @Override
    public Mono<String> getEqualInstallmentRoundingTo() {
        return port.getEqualInstallmentParams()
                .switchIfEmpty(Mono.just(
                        MetaProperty
                        .builder()
                        .params(List.of(
                                MetaPropertyParam
                                .builder()
                                .name(ROUNDING_TO.getValue())
                                .value("1")
                                .build()))
                        .build()))
                .map(metaProperty -> metaProperty.getParams().stream()
                        .filter(metaPropertyParam -> metaPropertyParam.getName().equals(ROUNDING_TO.getValue()))
                        .map(MetaPropertyParam::getValue)
                        .toList().get(0));
    }

    @Override
    public Mono<String> getEqualInstallmentRoundingLogic() {
        return port.getEqualInstallmentParams()
                .switchIfEmpty(Mono.just(
                        MetaProperty
                        .builder()
                        .params(List.of(
                                MetaPropertyParam
                                .builder()
                                .name(ROUNDING_LOGIC.getValue())
                                .value("HalfUp")
                                .build()))
                        .build()))
                .map(metaProperty -> metaProperty.getParams().stream()
                        .filter(metaPropertyParam -> metaPropertyParam.getName().equals(ROUNDING_LOGIC.getValue()))
                        .map(MetaPropertyParam::getValue)
                        .toList().get(0));
    }

    @Override
    public Mono<MetaPropertyResponseDTO> getMetaPropertyByPropertyId(String propertyId) {
        ModelMapper mapper = new ModelMapper();
        return port
                .getMetaPropertyByPropertyIdAndStatus(propertyId, STATUS_ACTIVE.getValue())
                .defaultIfEmpty(MetaPropertyEntity.builder().build())
                .map(metaPropertyEntity -> mapper.map(metaPropertyEntity, MetaPropertyResponseDTO.class))
                .doOnNext(metaPropertyResponseDTO -> log.info("Meta property entity received : {}", metaPropertyResponseDTO));
    }

    @Override
    public Mono<CalculationMetaProperty> getCalculationMetaProperty() {
        return port.getMetaPropertyByPropertyIdAndStatus(Constants.CALCULATION_META_PROPERTY.getValue(), STATUS_ACTIVE.getValue())
                .map(metaPropertyEntity -> {
                    Gson gson = new Gson();
                    return gson.fromJson(metaPropertyEntity.getParameters(), CalculationMetaProperty.class);
                });
    }

    @Override
    public Mono<AccountingMetaProperty> getAccountingMetaProperty() {
        return port.getMetaPropertyByPropertyIdAndStatus(MetaPropertyEnum.ACCOUNTING_META_PROPERTY_ID.getValue(), STATUS_ACTIVE.getValue())
                .switchIfEmpty(Mono.just(MetaPropertyEntity.builder().build()))
                .map(metaPropertyEntity -> {
                    Gson gson = new Gson();
                    AccountingMetaProperty accountingMetaProperty;
                    if (metaPropertyEntity.getParameters() != null) {
                        accountingMetaProperty = gson.fromJson(metaPropertyEntity.getParameters(), AccountingMetaProperty.class);
                    } else {
                        accountingMetaProperty = AccountingMetaProperty
                                .builder()
                                .allowAdvanceJournal(false)
                                .allowSCProvision(false)
                                .build();
                    }
                    return accountingMetaProperty;
                });
    }

    @Override
    public Mono<LoanCalculationMetaProperty> getLoanCalculationMetaProperty(LocalDate businessDate) {
        return port.getMetaPropertyByPropertyIdAndStatus(MetaPropertyEnum.LOAN_CALCULATION_META_PROPERTY_ID.getValue(), STATUS_ACTIVE.getValue())
                .switchIfEmpty(Mono.just(MetaPropertyEntity.builder().build()))
                .flatMap(metaPropertyEntity -> {
                    Gson gson = new Gson();
                    LoanCalculationMetaProperty loanCalculationMetaProperty;

                    if (metaPropertyEntity.getParameters() != null) {
                        loanCalculationMetaProperty = gson.fromJson(metaPropertyEntity.getParameters(), LoanCalculationMetaProperty.class);
                        log.info("Loan calculation meta property received : {}", loanCalculationMetaProperty);
                        if (loanCalculationMetaProperty.getDaysInYear() != null && loanCalculationMetaProperty.getDaysInYear().equalsIgnoreCase("Actual")) {
                            return Mono.just(businessDate.isLeapYear() ? 366 : 365)
                                    .map(daysInYear -> {
                                        loanCalculationMetaProperty.setDaysInYear(daysInYear.toString());
                                        loanCalculationMetaProperty.setServiceChargeDeductionMethod(
                                                loanCalculationMetaProperty.getServiceChargeDeductionMethod() == null
                                                        ? MetaPropertyEnum.SERVICE_CHARGE_DEDUCTION_METHOD_RATE_BASED.getValue()
                                                        : loanCalculationMetaProperty.getServiceChargeDeductionMethod());

                                        log.info("service charge deduction method set : {}", loanCalculationMetaProperty.getServiceChargeDeductionMethod());

                                        return loanCalculationMetaProperty;
                                    });
                        } else if (loanCalculationMetaProperty.getDaysInYear() == null){
                            return Mono.just(loanCalculationMetaProperty)
                                    .map(loanCalculationMetaProperty1 -> {
                                        loanCalculationMetaProperty.setDaysInYear("365");
                                        return loanCalculationMetaProperty;
                                    });
                        }

                    } else {
                        loanCalculationMetaProperty = LoanCalculationMetaProperty
                                .builder()
                                .serviceChargeRatePrecision(8)
                                .serviceChargeAmountPrecision(5)
                                .roundingLogic("HalfUp")
                                .daysInYear("365")
                                .serviceChargeDeductionMethod(MetaPropertyEnum.SERVICE_CHARGE_DEDUCTION_METHOD_RATE_BASED.getValue())
                                .build();
                        return Mono.just(loanCalculationMetaProperty);
                    }

                    return Mono.just(loanCalculationMetaProperty);
                });
    }

    @Override
    public Mono<EqualInstallmentMetaProperty> getEqualInstallmentMetaProperty() {
        return port.getMetaPropertyByPropertyIdAndStatus(MetaPropertyEnum.EQUAL_INSTALLMENT_META_PROPERTY_ID.getValue(), STATUS_ACTIVE.getValue())
                .switchIfEmpty(Mono.just(MetaPropertyEntity.builder().build()))
                .map(metaPropertyEntity -> {
                    Gson gson = new Gson();
                    EqualInstallmentMetaProperty equalInstallmentMetaProperty;
                    if (metaPropertyEntity.getParameters() != null) {
                        equalInstallmentMetaProperty = gson.fromJson(metaPropertyEntity.getParameters(), EqualInstallmentMetaProperty.class);
                        if (equalInstallmentMetaProperty.getMaxDeviationPercentage() == null || equalInstallmentMetaProperty.getMaxDeviationPercentage() <= 0.0) {
                            equalInstallmentMetaProperty.setMaxDeviationPercentage(50.00);
                        }
                    } else {
                        equalInstallmentMetaProperty = EqualInstallmentMetaProperty
                                .builder()
                                .installmentPrecision(2)
                                .serviceChargeRatePrecision(8)
                                .serviceChargePrecision(2)
                                .roundingToNearestInteger(1)
                                .roundingLogic("HalfUp")
                                .maxDeviationPercentage(50.00)
                                .build();
                    }
                    return equalInstallmentMetaProperty;
                });
    }
}
