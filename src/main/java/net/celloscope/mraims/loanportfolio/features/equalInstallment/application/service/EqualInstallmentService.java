package net.celloscope.mraims.loanportfolio.features.equalInstallment.application.service;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.enums.RepaymentScheduleEnum;
import net.celloscope.mraims.loanportfolio.features.equalInstallment.adapter.in.web.handler.dto.out.EqualInstallmentResponseDTO;
import net.celloscope.mraims.loanportfolio.features.equalInstallment.application.port.in.EqualInstallmentUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.out.MetaPropertyPersistencePort;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.service.MetaPropertyService;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.EqualInstallmentMetaProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Slf4j
@Service
public class EqualInstallmentService extends MetaPropertyService implements EqualInstallmentUseCase {

    public EqualInstallmentService(MetaPropertyPersistencePort port) {
        super(port);
    }

    private static double round(int scale, double amount, RoundingMode roundingMode) {
        return new BigDecimal(amount).setScale(scale, roundingMode).doubleValue();
    }

    @Override
    public Mono<EqualInstallmentResponseDTO> getEqualInstallmentAmount(Double loanAmount, Double serviceChargeRate, String serviceChargeRateFrequency, Integer noOfInstallments, String paymentPeriod, String daysInYear) {
        return this.getEqualInstallmentMetaProperty()
                .flatMap(equalInstallmentMetaProperty -> {
                    RoundingMode roundingMode = CommonFunctions.getRoundingMode(equalInstallmentMetaProperty.getRoundingLogic());
                    log.info("rounding Mode : {}", roundingMode);
                    int equalInstallmentPrecision = equalInstallmentMetaProperty.getInstallmentPrecision();
                    Double serviceChargeRatePerPeriod = getServiceChargeRatePerPeriod(serviceChargeRate, equalInstallmentPrecision, paymentPeriod, daysInYear, serviceChargeRateFrequency, roundingMode);
                    System.out.println("\nService Charge Rate Per Period : " + serviceChargeRatePerPeriod);

                    double probableEI = (loanAmount * serviceChargeRatePerPeriod) / (1 - Math.pow(1 + serviceChargeRatePerPeriod, -noOfInstallments));
                    probableEI = round(equalInstallmentPrecision, probableEI, roundingMode);
                    System.out.println("Probable EI : " + probableEI);

                    return getEI(loanAmount, serviceChargeRatePerPeriod, noOfInstallments, probableEI);
                });
    }

    @Override
    public Mono<EqualInstallmentResponseDTO> getEqualInstallmentAmountWhenPaymentPeriodUndefined(Double loanAmount, Double annualServiceChargeRate, Integer noOfInstallments, Integer loanTerm) {
        return this.getEqualInstallmentMetaProperty()
                .flatMap(equalInstallmentMetaProperty -> {
                    RoundingMode roundingMode = CommonFunctions.getRoundingMode(equalInstallmentMetaProperty.getRoundingLogic());
                    log.info("rounding Mode : {}", roundingMode);
                    int equalInstallmentPrecision = equalInstallmentMetaProperty.getInstallmentPrecision();
                    int serviceChargePrecision = equalInstallmentMetaProperty.getServiceChargeRatePrecision();

                    double annualServiceChargeRateMultipliedByLoanTerm = annualServiceChargeRate * getLoanTermInYears(loanTerm).doubleValue();
                    double serviceChargeRatePerPeriod = round(serviceChargePrecision, annualServiceChargeRateMultipliedByLoanTerm / noOfInstallments, roundingMode);
                    double probableEI = (loanAmount * serviceChargeRatePerPeriod) / (1 - Math.pow(1 + serviceChargeRatePerPeriod, -noOfInstallments));
                    Tuple3<BigDecimal, BigDecimal, BigDecimal> eiTuple = this.getRoundedEI(loanAmount, serviceChargeRatePerPeriod,
                            noOfInstallments, probableEI, String.valueOf(equalInstallmentMetaProperty.getRoundingToNearestInteger()),
                            equalInstallmentMetaProperty.getMaxDeviationPercentage());
                    probableEI = round(equalInstallmentPrecision, eiTuple.getT1().doubleValue(), roundingMode);
                    System.out.println("\nProbable EI : " + probableEI);

                    return getEI(loanAmount, serviceChargeRatePerPeriod, noOfInstallments, probableEI);
                });
    }

    private BigDecimal getLoanTermInYears(Integer loanTermInMonth) {
        if (loanTermInMonth == null)
            return BigDecimal.ONE;
        return BigDecimal.valueOf(loanTermInMonth).divide(BigDecimal.valueOf(12), 12, RoundingMode.HALF_UP);
    }

    @Override
    public Mono<EqualInstallmentResponseDTO> getEqualInstallmentAmountWhenPaymentPeriodUndefinedV2(BigDecimal loanAmount, BigDecimal annualServiceChargeRate, Integer noOfInstallments, Integer roundingToNextInteger, Integer loanTermInMonths) {
        return this.getEqualInstallmentMetaProperty()
                .flatMap(equalInstallmentMetaProperty -> {
                    RoundingMode roundingMode = CommonFunctions.getRoundingMode(equalInstallmentMetaProperty.getRoundingLogic());
                    log.info("rounding Mode : {}", roundingMode);
                    int equalInstallmentPrecision = equalInstallmentMetaProperty.getInstallmentPrecision();
                    int serviceChargePrecision = equalInstallmentMetaProperty.getServiceChargeRatePrecision();
                    /*double serviceChargeRatePerPeriod = round(serviceChargePrecision, annualServiceChargeRate.doubleValue() / noOfInstallments, roundingMode);
                    double probableEI = (loanAmount.doubleValue() * serviceChargeRatePerPeriod) / (1 - Math.pow(1 + serviceChargeRatePerPeriod, -noOfInstallments));
                    probableEI = round(equalInstallmentPrecision, probableEI, roundingMode);*/

                    double annualServiceChargeRateMultipliedByLoanTerm = annualServiceChargeRate.multiply(getLoanTermInYears(loanTermInMonths)).doubleValue();
                    double serviceChargeRatePerPeriod = round(serviceChargePrecision, annualServiceChargeRateMultipliedByLoanTerm / noOfInstallments, roundingMode);
                    double probableEI = (loanAmount.doubleValue() * serviceChargeRatePerPeriod) / (1 - Math.pow(1 + serviceChargeRatePerPeriod, -noOfInstallments));
                    probableEI = round(equalInstallmentPrecision, probableEI, roundingMode);
                    System.out.println("\nProbable EI : " + probableEI);

                    return getEIV2(loanAmount.doubleValue(), serviceChargeRatePerPeriod, noOfInstallments, probableEI, roundingToNextInteger);
                });
    }

    @Override
    public Mono<EqualInstallmentResponseDTO> getEqualInstallmentAmountAccordingToInterestCalcMethod(BigDecimal loanAmount, BigDecimal annualServiceChargeRate, Integer noOfInstallments, Integer loanTerm, String interestCalcMethod) {
        return interestCalcMethod.equals(RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_FLAT.getValue())
                ? this.getEqualInstallmentAmountFlat(loanAmount, annualServiceChargeRate, noOfInstallments, loanTerm)
                    .map(eiTuple -> EqualInstallmentResponseDTO
                            .builder()
                            .equalInstallmentAmount(eiTuple.getT1())
                            .minimumEqualInstallmentAmount(eiTuple.getT2())
                            .maximumEqualInstallmentAmount(eiTuple.getT3())
                            .build())
                : this.getEqualInstallmentAmountWhenPaymentPeriodUndefined(loanAmount.doubleValue(), annualServiceChargeRate.doubleValue(), noOfInstallments, loanTerm);
    }

    Mono<Tuple3<BigDecimal, BigDecimal, BigDecimal>> getEqualInstallmentAmountFlat(
            BigDecimal loanAmount, BigDecimal annualServiceChargeRate, Integer noOfInstallments, Integer loanTerm) {
        return this.getEqualInstallmentMetaProperty()
                .flatMap(equalInstallmentMetaProperty -> {
                    log.info("Added one line for demo");
                    RoundingMode roundingMode = CommonFunctions.getRoundingMode(equalInstallmentMetaProperty.getRoundingLogic());
                    log.info("Rounding Mode from meta property: {}", roundingMode);

                    int equalInstallmentPrecision = equalInstallmentMetaProperty.getInstallmentPrecision();
                    Integer roundingToNearestInteger = equalInstallmentMetaProperty.getRoundingToNearestInteger();
                    log.info("Equal Installment Precision from meta property: {}", equalInstallmentPrecision);

                    BigDecimal annualServiceChargeRateMultipliedByLoanTerm = annualServiceChargeRate.multiply(getLoanTermInYears(loanTerm));
                    BigDecimal totalServiceCharge = loanAmount.multiply(annualServiceChargeRateMultipliedByLoanTerm.add(BigDecimal.ONE)).subtract(loanAmount);
                    BigDecimal cumulativeAmount = loanAmount.add(totalServiceCharge);

                    BigDecimal calculatedEi = cumulativeAmount.divide(BigDecimal.valueOf(noOfInstallments), equalInstallmentPrecision, roundingMode);

                    log.info("Annual Service Charge Rate Multiplied by Loan Term: {}", annualServiceChargeRateMultipliedByLoanTerm);
                    log.info("Total Service Charge: {}", totalServiceCharge);
                    log.info("Cumulative Amount: {}", cumulativeAmount);
                    log.info("Calculated EI: {}", calculatedEi);

                    int roundingTo = (roundingToNearestInteger != null && roundingToNearestInteger > 0) ? roundingToNearestInteger : 1;
                    log.info("Rounding EI to nearest integer: {}", roundingTo);


                    BigDecimal maximumEI = CommonFunctions.getMaximumEqualInstallmentAmountFlat(calculatedEi, noOfInstallments, cumulativeAmount);
                    BigDecimal minimumEI = CommonFunctions.getMinimumEqualInstallmentAmountFlat(calculatedEi, noOfInstallments, cumulativeAmount, equalInstallmentMetaProperty.getMaxDeviationPercentage());

                    // Return a Tuple3 containing the calculated EI, rounded lower limit, and rounded upper limit
                    return Mono.just(Tuples.of(calculatedEi, maximumEI, minimumEI));
                });
    }


    private Double getServiceChargeRatePerPeriod(Double serviceChargeRate, Integer serviceChargePrecision, String paymentPeriod, String daysInYear, String serviceChargeRateFrequency, RoundingMode roundingMode) {
        double serviceChargeRatePerPeriod = 0.0;
        if (serviceChargeRateFrequency.equalsIgnoreCase("YEARLY")) {
            if (daysInYear == null) {
                switch (paymentPeriod.toUpperCase()) {
                    case "MONTH" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * (1.0 / 12.0), roundingMode);
                    case "WEEK" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * (1.0 / 52.0), roundingMode);
                    case "HALF-YEAR" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * (1.0 / 2.0), roundingMode);
                }
            } else {
                switch (paymentPeriod.toUpperCase()) {
                    // TODO: 4/10/23 1 Month = How Many Days? Considered 30 for now.
                    case "MONTH" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * (30.0 / Double.parseDouble(daysInYear)), roundingMode);
                    case "WEEK" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * (7.0 / Double.parseDouble(daysInYear)), roundingMode);
                    case "HALF-YEAR" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * (180.0 / Double.parseDouble(daysInYear)), roundingMode);
                }
            }
        }

        if (serviceChargeRateFrequency.equalsIgnoreCase("MONTHLY")) {
            if (daysInYear == null) {
                switch (paymentPeriod.toUpperCase()) {
                    case "MONTH" -> serviceChargeRatePerPeriod = serviceChargeRate;
                    case "WEEK" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * (1.0 / 4.0), roundingMode);
                    case "HALF-YEAR" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * 6.0, roundingMode);
                    /*case "YEAR" -> serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * 12.0, roundingMode);*/
                }
            } else {
                switch (paymentPeriod.toUpperCase()) {
                    case "MONTH" -> serviceChargeRatePerPeriod = serviceChargeRate;
                    case "WEEK" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, ((serviceChargeRate * 12.0) / Double.parseDouble(daysInYear)) * 7, roundingMode);
                    case "HALF-YEAR" ->
                            serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * 6.0, roundingMode);
                    /*case "YEAR" -> serviceChargeRatePerPeriod = round(serviceChargePrecision, serviceChargeRate * 12.0, roundingMode);*/
                }
            }
        }
        return serviceChargeRatePerPeriod;
    }

    private Mono<RoundingMode> getRoundingMode() {
        return  this.getEqualInstallmentMetaProperty()
                .map(EqualInstallmentMetaProperty::getRoundingLogic)
                .map(roundingLogic -> {
                    log.info("Into getRoundingMode with RoundingLogic : {}", roundingLogic);
                    RoundingMode roundingMode = null;
                    switch (roundingLogic.toUpperCase()) {
                        case "HALFUP" -> roundingMode = RoundingMode.HALF_UP;
                        case "HALFDOWN" -> roundingMode = RoundingMode.HALF_DOWN;
                        case "UP" -> roundingMode = RoundingMode.UP;
                        case "DOWN" -> roundingMode = RoundingMode.DOWN;
                    }
                    return roundingMode;
                });
    }

    private Mono<EqualInstallmentResponseDTO> getEI(Double loanAmount, Double serviceChargeRatePerPeriod, Integer noOfInstallments, Double probableEI) {
        return  this.getEqualInstallmentMetaProperty()
                .map(equalInstallmentMetaProperty -> {
                    Integer roundingToNearestInteger = equalInstallmentMetaProperty.getRoundingToNearestInteger();
                    Tuple3<BigDecimal, BigDecimal, BigDecimal> eiTuple = getRoundedEI(loanAmount, serviceChargeRatePerPeriod,
                            noOfInstallments, probableEI, roundingToNearestInteger.toString(), equalInstallmentMetaProperty.getMaxDeviationPercentage());

                    return EqualInstallmentResponseDTO
                            .builder()
                            .equalInstallmentAmount(eiTuple.getT1())
                            .minimumEqualInstallmentAmount(eiTuple.getT2())
                            .maximumEqualInstallmentAmount(eiTuple.getT3())
                            .serviceChargeRatePerPeriod(BigDecimal.valueOf(serviceChargeRatePerPeriod))
                            .build();
                });
    }



    private Mono<EqualInstallmentResponseDTO> getEIV2(Double loanAmount, Double serviceChargeRatePerPeriod, Integer noOfInstallments, Double probableEI, Integer roundingToNextInteger) {
        return  this.getEqualInstallmentMetaProperty()
                .map(equalInstallmentMetaProperty -> {
                    Integer roundingTo = equalInstallmentMetaProperty.getRoundingToNearestInteger();
                    roundingTo = roundingToNextInteger > 0 ? roundingToNextInteger : roundingTo;
                    Tuple3<BigDecimal, BigDecimal, BigDecimal> eiTuple = getRoundedEI(loanAmount, serviceChargeRatePerPeriod,
                            noOfInstallments, probableEI, roundingTo.toString(), equalInstallmentMetaProperty.getMaxDeviationPercentage());

                    return EqualInstallmentResponseDTO
                            .builder()
                            .equalInstallmentAmount(eiTuple.getT1())
                            .minimumEqualInstallmentAmount(eiTuple.getT2())
                            .maximumEqualInstallmentAmount(eiTuple.getT3())
                            .serviceChargeRatePerPeriod(BigDecimal.valueOf(serviceChargeRatePerPeriod))
                            .roundingToNextInteger(roundingTo)
                            .build();
                });
    }


    private Tuple3<BigDecimal, BigDecimal, BigDecimal> getRoundedEI(
            Double loanAmount, Double serviceChargeRatePerPeriod, Integer noOfInstallments,
            Double probableEI, String roundingToNearestInteger, Double maxDeviation) {

        int count = 0;
        double remainingBalance = loanAmount;
        BigDecimal selectedEI = BigDecimal.ZERO;

        if (roundingToNearestInteger != null && roundingToNearestInteger.equals(RepaymentScheduleEnum.NO_ROUNDING_TO_INTEGER.getValue())) {
            return Tuples.of(BigDecimal.valueOf(probableEI), BigDecimal.valueOf(probableEI), BigDecimal.valueOf(probableEI));
        }

        Integer roundingTo = (roundingToNearestInteger != null && Integer.parseInt(roundingToNearestInteger) > 0)
                ? Integer.parseInt(roundingToNearestInteger)
                : 1;

        BigDecimal probableEIRoundedUp = BigDecimal.valueOf(roundingTo * (Math.ceil(probableEI / roundingTo)));
        BigDecimal probableEIRoundedDown = BigDecimal.valueOf(roundingTo * (Math.floor(probableEI / roundingTo)));

        while (remainingBalance > 0 && count < noOfInstallments) {
            double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
            double calculatedPrincipal = probableEIRoundedUp.doubleValue() - interestAmount;
            remainingBalance = remainingBalance - calculatedPrincipal;
            count++;
            selectedEI = probableEIRoundedUp;
        }

        log.info("RoundedUp EI : {} | Installment count : {}",probableEIRoundedUp, count);
        while (count != noOfInstallments) {
            double interestAmount = remainingBalance * serviceChargeRatePerPeriod;
            double calculatedPrincipal = probableEIRoundedDown.doubleValue() - interestAmount;
            remainingBalance = remainingBalance - calculatedPrincipal;
            count++;
            selectedEI = probableEIRoundedDown;
        }

        log.info("RoundedDown EI : {} | Installment count : {}",probableEIRoundedDown, count);
        BigDecimal maximumEI = CommonFunctions.getMaximumEqualInstallmentAmountDeclining(selectedEI, noOfInstallments, BigDecimal.valueOf(loanAmount), serviceChargeRatePerPeriod);
        BigDecimal minimumEI = CommonFunctions.getMinimumEqualInstallmentAmountDeclining(selectedEI, noOfInstallments, BigDecimal.valueOf(loanAmount), serviceChargeRatePerPeriod, maxDeviation);

        log.info("Selected EI : {} | Minimum EI : {} | Maximum EI : {}", selectedEI, minimumEI, maximumEI);
        return Tuples.of(selectedEI, minimumEI, maximumEI);
    }


}
