package net.celloscope.mraims.loanportfolio.features.loancalculator.application.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.CommonFunctions;
import net.celloscope.mraims.loanportfolio.core.util.MetaProperty;
import net.celloscope.mraims.loanportfolio.core.util.enums.RepaymentScheduleEnum;
import net.celloscope.mraims.loanportfolio.core.util.enums.Status;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.equalInstallment.adapter.in.web.handler.dto.out.EqualInstallmentResponseDTO;
import net.celloscope.mraims.loanportfolio.features.equalInstallment.application.port.in.EqualInstallmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.LoanCalculatorUseCase;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.InstallmentInfo;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.request.LoanCalculatorRequestDTO;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.response.*;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.MetaPropertyResponseDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.EqualInstallmentMetaProperty;
import net.celloscope.mraims.loanportfolio.features.metaproperty.domain.MetaPropertyEnum;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleViewDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.LoanRepaymentScheduleRequestDTO;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.function.Predicate;

@Service
@Slf4j
public class LoanCalculatorService implements LoanCalculatorUseCase {

    private final CommonRepository commonRepository;
    private final ModelMapper modelMapper;
    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final MetaPropertyUseCase metaPropertyUseCase;
    private final EqualInstallmentUseCase equalInstallmentUseCase;

    public LoanCalculatorService(CommonRepository commonRepository, ModelMapper modelMapper, LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase, MetaPropertyUseCase metaPropertyUseCase, EqualInstallmentUseCase equalInstallmentUseCase) {
        this.commonRepository = commonRepository;
        this.modelMapper = modelMapper;
        this.loanRepaymentScheduleUseCase = loanRepaymentScheduleUseCase;
        this.metaPropertyUseCase = metaPropertyUseCase;
        this.equalInstallmentUseCase = equalInstallmentUseCase;
    }

    @Override
    public Mono<LoanProductListResponseDTO> getActiveLoanProductsByMfi(String instituteOid) {
        return commonRepository
                .getAllLoanProductsByMfi(instituteOid, Status.STATUS_ACTIVE.getValue())
                /*.filter(loanProductEntity -> loanProductEntity.getInterestCalcMethod()
                        .equalsIgnoreCase(RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE_EQUAL_INSTALLMENT.getValue()))*/
                .doOnRequest(l -> log.info("Fetching loan products for MFI with instituteOid: {}", instituteOid))
                .map(loanProductEntity -> modelMapper.map(loanProductEntity, LoanProductDTO.class))
                .collectList()
                .doOnSuccess(loanProductList -> log.info("Loan products fetched successfully for MFI with instituteOid: {} of size : {}", instituteOid, loanProductList.size()))
                .map(loanProductList -> LoanProductListResponseDTO
                        .builder()
                        .loanProductList(loanProductList)
                        .userMessage("Loan products fetched successfully")
                        .build());
    }

    @Override
    public Mono<LoanProductInfoResponseDTO> getLoanProductInfo(String loanProductId) {
        return commonRepository
                .getLoanProductEntityByLoanProductId(loanProductId)
                .doOnRequest(l -> log.info("Fetching loan product info for loanProductId: {}", loanProductId))
                .doOnNext(loanProductEntity -> log.info("Loan product info fetched successfully for loanProductId: {}", loanProductId))
                .zipWith(commonRepository
                        .getServiceChargeChartEntityByLoanProductId(loanProductId)
                        .doOnRequest(l -> log.info("Fetching service charge info for loanProductId: {}", loanProductId))
                        .doOnNext(serviceChargeChartEntity -> log.info("Service charge info fetched successfully for loanProductId: {}", loanProductId)))
                .map(tuple -> LoanProductInfoResponseDTO
                        .builder()
                        .loanProductDTO(modelMapper.map(tuple.getT1(), LoanProductDTO.class))
                        .serviceChargeInfoDTO(modelMapper.map(tuple.getT2(), ServiceChargeInfoDTO.class))
                        .userMessage("Loan product info fetched successfully")
                        .build());
    }

    @Override
    public Mono<LoanCalculatorResponseDTO> generateRepaymentScheduleForLoan(LoanCalculatorRequestDTO requestDTO) {
        return this.getLoanProductInfo(requestDTO.getLoanProductId())
                .flatMap(loanProductInfoResponseDTO -> this.validateLoanCalculatorRequestDTO(requestDTO, loanProductInfoResponseDTO)
                        .map(loanCalculatorRequestDTO -> loanProductInfoResponseDTO))
                .flatMap(loanProductInfoResponseDTO -> this.getEqualInstallmentMetaProperty().zipWith(Mono.just(loanProductInfoResponseDTO)))
                .flatMap(tuple2 -> {
                    EqualInstallmentMetaProperty metaProperty = tuple2.getT1();
                    LoanProductInfoResponseDTO loanProductInfoResponseDTO = tuple2.getT2();
                    RoundingMode roundingMode = CommonFunctions.getRoundingMode(metaProperty.getRoundingLogic());

                    Mono<LoanCalculatorResponseDTO> responseMono = Mono.just(LoanCalculatorResponseDTO.builder().build());

                    if (loanProductInfoResponseDTO.getLoanProductDTO().getInterestCalcMethod().equalsIgnoreCase(RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE_EQUAL_INSTALLMENT.getValue())) {
                        responseMono = getRepaymentScheduleForDeclining(requestDTO, loanProductInfoResponseDTO, metaProperty, roundingMode)
                                .doOnError(e -> log.error("Error generating repayment schedule for loan Declining: {}", e.getMessage()));
                    } else if (loanProductInfoResponseDTO.getLoanProductDTO().getInterestCalcMethod().equalsIgnoreCase(RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_FLAT.getValue())) {
                        responseMono = getRepaymentScheduleForFlat(requestDTO, loanProductInfoResponseDTO, metaProperty)
                                .doOnError(e -> log.error("Error generating repayment schedule for loan Flat: {}", e.getMessage()));
                    }

                    return responseMono;
                });
    }

    private Mono<LoanCalculatorResponseDTO> getRepaymentScheduleForDeclining(LoanCalculatorRequestDTO requestDTO, LoanProductInfoResponseDTO loanProductInfoResponseDTO, EqualInstallmentMetaProperty metaProperty, RoundingMode roundingMode) {
        return this.getEqualInstallmentAndServiceChargeRatePerPeriodAndRoundingToNextInteger(requestDTO, loanProductInfoResponseDTO.getServiceChargeInfoDTO(), metaProperty)
                .doOnNext(equalInstallmentResponseDTO -> log.info("equal installment response dto test : {}", equalInstallmentResponseDTO))
                .flatMap(equalInstallmentResponseDTO -> loanRepaymentScheduleUseCase
                        .viewRepaymentScheduleForLoanCalculator(
                                LoanRepaymentScheduleRequestDTO
                                        .builder()
                                        .loanAmount(requestDTO.getLoanAmount())
                                        .installmentAmount(equalInstallmentResponseDTO.getEqualInstallmentAmount())
                                        .serviceChargeRatePerPeriod(equalInstallmentResponseDTO.getServiceChargeRatePerPeriod())
                                        .noOfInstallments(requestDTO.getNoOfInstallments())
                                        .repaymentFrequency(loanProductInfoResponseDTO.getLoanProductDTO().getRepaymentFrequency())
                                        .graceDays(requestDTO.getGraceDays())
                                        .disburseDate(requestDTO.getDisbursementDate())
                                        .samityDay(requestDTO.getSamityDay())
                                        .roundingLogic(metaProperty.getRoundingLogic())
                                        .monthlyRepaymentFrequencyDay(loanProductInfoResponseDTO.getLoanProductDTO().getMonthlyRepayDay())
                                        .officeId(requestDTO.getOfficeId())
                                        .build())
                        .doOnRequest(l -> log.info("Generating repayment schedule for loan with loanProductId: {}", requestDTO.getLoanProductId()))
                        .doOnNext(repaymentScheduleViewDTOList -> log.info("Repayment schedule generated successfully for loan with loanProductId: {}", requestDTO.getLoanProductId()))
                        .map(repaymentScheduleViewDTOS -> LoanCalculatorResponseDTO
                                .builder()
                                .calculatedInstallmentAmount(repaymentScheduleViewDTOS.get(0).getScheduledPayment())
                                .selectedInstallmentAmount(equalInstallmentResponseDTO.getEqualInstallmentAmount())
                                .installmentRoundedToNextInteger(equalInstallmentResponseDTO.getRoundingToNextInteger())
                                .serviceChargeRatePerPeriod(equalInstallmentResponseDTO.getServiceChargeRatePerPeriod())
                                .lastInstallmentAmount(repaymentScheduleViewDTOS.get(repaymentScheduleViewDTOS.size() - 1).getTotalPayment())
                                .totalServiceCharge(repaymentScheduleViewDTOS.stream().map(RepaymentScheduleViewDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add))
                                .data(repaymentScheduleViewDTOS)
                                .userMessage("Repayment schedule generated successfully")
                                .build())
                );
    }

    private Mono<LoanCalculatorResponseDTO> getRepaymentScheduleForFlat(LoanCalculatorRequestDTO requestDTO, LoanProductInfoResponseDTO loanProductInfoResponseDTO, EqualInstallmentMetaProperty metaProperty) {

        int roundingToNearestInteger = requestDTO.getRoundingToNearestInteger() > 0
                ? requestDTO.getRoundingToNearestInteger()
                : metaProperty.getRoundingToNearestInteger();
        return this.getEqualInstallmentAndServiceChargeRatePerPeriodAndRoundingToNextInteger(requestDTO, loanProductInfoResponseDTO.getServiceChargeInfoDTO(), metaProperty)
                .doOnNext(equalInstallmentResponseDTO -> log.info("equal installment response dto test : {}", equalInstallmentResponseDTO))
                .flatMap(equalInstallmentResponseDTO -> loanRepaymentScheduleUseCase
                        .viewRepaymentScheduleFlat(
                                LoanRepaymentScheduleRequestDTO
                                        .builder()
                                        .officeId(requestDTO.getOfficeId())
                                        .loanAmount(requestDTO.getLoanAmount())
                                        .serviceChargeRate(loanProductInfoResponseDTO.getServiceChargeInfoDTO().getServiceChargeRate())
                                        .serviceChargeRateFrequency(loanProductInfoResponseDTO.getServiceChargeInfoDTO().getServiceChargeRateFreq())
                                        .noOfInstallments(requestDTO.getNoOfInstallments())
                                        .repaymentFrequency(loanProductInfoResponseDTO.getLoanProductDTO().getRepaymentFrequency())
                                        .graceDays(requestDTO.getGraceDays())
                                        .disburseDate(requestDTO.getDisbursementDate())
                                        .samityDay(requestDTO.getSamityDay())
                                        .roundingLogic(metaProperty.getRoundingLogic())
                                        .roundingToNearestIntegerLogic(metaProperty.getRoundingLogic())
                                        .roundingToNearest(roundingToNearestInteger)
                                        .monthlyRepaymentFrequencyDay(loanProductInfoResponseDTO.getLoanProductDTO().getMonthlyRepayDay())
                                        .serviceChargeRatePrecision(metaProperty.getServiceChargeRatePrecision())
                                        .serviceChargeAmountPrecision(metaProperty.getServiceChargePrecision())
                                        .installmentPrecision(metaProperty.getInstallmentPrecision())
                                        .installmentAmount(requestDTO.getInstallmentAmount())
                                        .build())
                        .doOnRequest(l -> log.info("Generating repayment schedule for loan with loanProductId: {}", requestDTO.getLoanProductId()))
                        .doOnNext(repaymentScheduleViewDTOList -> log.info("Repayment schedule generated successfully for loan with loanProductId: {}", requestDTO.getLoanProductId()))
                        .map(repaymentScheduleViewDTOS -> LoanCalculatorResponseDTO
                                .builder()
                                .calculatedInstallmentAmount(repaymentScheduleViewDTOS.get(0).getScheduledPayment())
                                .selectedInstallmentAmount(equalInstallmentResponseDTO.getEqualInstallmentAmount())
                                .installmentRoundedToNextInteger(equalInstallmentResponseDTO.getRoundingToNextInteger())
                                .serviceChargeRatePerPeriod(equalInstallmentResponseDTO.getServiceChargeRatePerPeriod())
                                .lastInstallmentAmount(repaymentScheduleViewDTOS.get(repaymentScheduleViewDTOS.size() - 1).getTotalPayment())
                                .totalServiceCharge(repaymentScheduleViewDTOS.stream().map(RepaymentScheduleViewDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add))
                                .data(repaymentScheduleViewDTOS)
                                .userMessage("Repayment schedule generated successfully")
                                .build())
                );
    }

    private Mono<EqualInstallmentResponseDTO> getEqualInstallmentAndServiceChargeRatePerPeriodAndRoundingToNextInteger(LoanCalculatorRequestDTO requestDTO, ServiceChargeInfoDTO serviceChargeInfoDTO, EqualInstallmentMetaProperty metaProperty) {

        BigDecimal annualServiceChargeRate = CommonFunctions.getAnnualInterestRate(serviceChargeInfoDTO.getServiceChargeRate(), serviceChargeInfoDTO.getServiceChargeRateFreq());
        RoundingMode roundingMode = CommonFunctions.getRoundingMode(metaProperty.getRoundingLogic());
        annualServiceChargeRate = annualServiceChargeRate.divide(BigDecimal.valueOf(100), metaProperty.getServiceChargeRatePrecision(), roundingMode);

        return requestDTO.getInstallmentAmount() != null && requestDTO.getInstallmentAmount().compareTo(BigDecimal.ZERO) > 0
                ? Mono.just(EqualInstallmentResponseDTO
                    .builder()
                    .equalInstallmentAmount(requestDTO.getInstallmentAmount())
                    .serviceChargeRatePerPeriod(round(metaProperty.getServiceChargeRatePrecision(), annualServiceChargeRate.doubleValue() / requestDTO.getNoOfInstallments(), roundingMode))
                    .build())
                : equalInstallmentUseCase
                    .getEqualInstallmentAmountWhenPaymentPeriodUndefinedV2(requestDTO.getLoanAmount(), annualServiceChargeRate, requestDTO.getNoOfInstallments(), requestDTO.getRoundingToNearestInteger(), requestDTO.getLoanTermInMonths());
    }

    private static BigDecimal round(int scale, double amount, RoundingMode roundingMode) {
        return new BigDecimal(amount).setScale(scale, roundingMode);
    }


    private Mono<EqualInstallmentMetaProperty> getEqualInstallmentMetaProperty() {
       /* return metaPropertyUseCase
                .getMetaPropertyByDescription(MetaPropertyEnum.EQUAL_INSTALLMENT.getValue())
                .map(metaPropertyResponseDTO -> metaPropertyResponseDTO.getParameters() == null
                        ? MetaPropertyResponseDTO
                            .builder()
                            .parameters("[{\"name\":\"InstallmentPrecision\",\"value\":\"2\"},{\"name\":\"ServiceChargePrecision\",\"value\":\"8\"},{\"name\":\"RoundingTo\",\"value\":\"1\"},{\"name\":\"RoundingLogic\",\"value\":\"HalfUp\"}]")
                            .build()
                        : metaPropertyResponseDTO)
                .map(MetaPropertyResponseDTO::getParameters)
                .map(CommonFunctions::getMetaPropertyFromJson);*/
        return metaPropertyUseCase.getEqualInstallmentMetaProperty();
    }

    private Mono<LoanCalculatorRequestDTO> validateLoanCalculatorRequestDTO(LoanCalculatorRequestDTO requestDTO, LoanProductInfoResponseDTO loanProductInfoResponseDTO) {
        Gson gson = new Gson();
        InstallmentInfo installmentInfo = gson.fromJson(loanProductInfoResponseDTO.getLoanProductDTO().getInstallmentInfoJson(), InstallmentInfo.class);

        validateSamityDay(requestDTO.getSamityDay());
        if (requestDTO.getLoanAmount().compareTo(loanProductInfoResponseDTO.getLoanProductDTO().getMinLoanAmount()) < 0) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT, "Loan amount is less than minimum loan amount"));
        } else if (requestDTO.getLoanAmount().compareTo(loanProductInfoResponseDTO.getLoanProductDTO().getMaxLoanAmount()) > 0) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT, "Loan amount is greater than maximum loan amount"));
        } else if (requestDTO.getNoOfInstallments() < installmentInfo.getMinInstallmentNo()) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT, "Number of installments is less than minimum installment number"));
        } else if (requestDTO.getNoOfInstallments() > installmentInfo.getMaxInstallmentNo()) {
            return Mono.error(new ExceptionHandlerUtil(HttpStatus.CONFLICT, "Number of installments is greater than maximum installment number"));
        } else {
            return Mono.just(requestDTO);
        }
    }

    private void validateSamityDay(String samityDay) {
        try {
            if (samityDay == null || samityDay.isEmpty()) {
                throw new IllegalArgumentException("Samity day is required");
            }

            DayOfWeek dayOfWeek = DayOfWeek.valueOf(samityDay.toUpperCase());

            if (dayOfWeek == DayOfWeek.FRIDAY) {
                throw new IllegalArgumentException("Samity day cannot be Friday");
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid samity day: " + samityDay + ". " + e.getMessage());
        }
    }
}
