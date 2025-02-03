package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.service;

import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.LoanRebateDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleViewDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.LoanRepaymentScheduleRequestDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RepaymentScheduleCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static net.celloscope.mraims.loanportfolio.repaymentSchedule.application.service.RepaymentScheduleServiceTest.*;

//@SpringBootTest
class LoanRepaymentScheduleServiceTest {
    /*@Autowired
    private LoanRepaymentScheduleService service;

    *//*LoanRepaymentScheduleServiceTest(LoanRepaymentScheduleService service) {
        this.service = service;
    }*//*

    @Test
    void contextLoading() {
        Assertions.assertNotNull(service);
        Assertions.assertNotNull(LoanRepaymentScheduleService.class);
    }


    @Test
    void getRepaymentDates() {
    }

    @ParameterizedTest
    @MethodSource("equalInstallmentRepaymentScheduleParamProvider")
    void viewRepaymentScheduleForLoan(BigDecimal loanAmount, BigDecimal serviceChargeRate, String serviceChargeRateFrequency, Integer noOfInstallments, String repaymentFrequency, Integer graceDays, LocalDate disburseDate, String samityDay, String roundingLogic, Integer roundingToNearest, Integer monthlyRepaymentFrequencyDay) {


        Mono<List<RepaymentScheduleViewDTO>> repaymentSchedule = service.viewRepaymentScheduleForLoan(
                LoanRepaymentScheduleRequestDTO
                        .builder()
                        .loanAmount(loanAmount)
                        .serviceChargeRate(serviceChargeRate)
                        .serviceChargeRateFrequency(serviceChargeRateFrequency)
                        .noOfInstallments(noOfInstallments)
                        .repaymentFrequency(repaymentFrequency)
                        .graceDays(graceDays)
                        .disburseDate(disburseDate)
                        .samityDay(samityDay)
                        .roundingLogic(roundingLogic)
                        .roundingToNearest(roundingToNearest)
                        .monthlyRepaymentFrequencyDay(monthlyRepaymentFrequencyDay)
                        .build());

        printRepaymentScheduleWithDates(repaymentSchedule.block());

    }



    @ParameterizedTest
    @MethodSource("rebatedRepaymentScheduleArgProvider")
    void getRebatedRepaymentSchedules(List<RepaymentSchedule> repaymentScheduleList, LoanRebateDTO loanRebateDTO) {
        List<RepaymentSchedule> repaymentSchedule = service.getRebatedRepaymentSchedules(repaymentScheduleList, loanRebateDTO);

        System.out.printf("Rebated Repayment Schedule: %s%n", repaymentSchedule);
        *//*ModelMapper modelMapper = new ModelMapper();
        List<RepaymentScheduleViewDTO> list = repaymentSchedule.stream().map(repaymentSchedule1 -> modelMapper.map(repaymentSchedule1, RepaymentScheduleViewDTO.class)).toList();
        printRepaymentScheduleWithDates(list);*//*
    }

    private static Stream<? extends Arguments> rebatedRepaymentScheduleArgProvider() {
        return Stream.of(
                Arguments.of(
                        new ArrayList<>()
                        {{
                            add(RepaymentSchedule
                                    .builder()
                                    .installNo(1)
                                    .serviceCharge(BigDecimal.valueOf(20))
                                    .build());
                            add(RepaymentSchedule
                                    .builder()
                                    .installNo(2)
                                    .serviceCharge(BigDecimal.valueOf(20))
                                    .build());
                            add(RepaymentSchedule
                                    .builder()
                                    .installNo(3)
                                    .serviceCharge(BigDecimal.valueOf(20))
                                    .build());
                        }},
                LoanRebateDTO
                        .builder()
                        .rebateAmount(BigDecimal.valueOf(50))
                        .managementProcessId("managementProcessId")
                        .loanAccountId("loanAccountId")
                        .build()
                )
        );
    }


    @ParameterizedTest
    @MethodSource("flatRepaymentScheduleParamProvider")
    void viewRepaymentScheduleFlat(String officeId, BigDecimal loanAmount, BigDecimal serviceChargeRate, String serviceChargeRateFrequency, Integer noOfInstallments, String repaymentFrequency, Integer graceDays, LocalDate disburseDate, String samityDay, String roundingMode, String roundingLogic, Integer roundingToNearest, Integer monthlyRepaymentFrequencyDay, Integer serviceChargeRatePrecision, Integer principalAmountPrecision, Integer installmentAmountPrecision) {

        Mono<List<RepaymentScheduleViewDTO>> repaymentSchedule = service.viewRepaymentScheduleFlat(officeId, loanAmount, serviceChargeRate, serviceChargeRateFrequency, noOfInstallments, repaymentFrequency, graceDays, disburseDate, samityDay, roundingMode, roundingLogic, roundingToNearest, monthlyRepaymentFrequencyDay, serviceChargeRatePrecision, principalAmountPrecision, installmentAmountPrecision, null);

        printFlatRepaymentSchedule(repaymentSchedule.block());
    }



    private static Stream<? extends Arguments> flatRepaymentScheduleParamProvider() {
        return Stream.of(
                Arguments.of(
                        "1018",
                        BigDecimal.valueOf(60000),
                        BigDecimal.valueOf(12.5),
                        "Yearly",
                        45,
                        "Weekly",
                        15,
                        LocalDate.of(2023,9,17),
                        "SATURDAY",
                        "No_Rounding", //rounding mode
//                        "Up", // rounding mode
                        "No_Rounding_To_Integer", // nearest integer rounding logic (installment amount)
//                        "UP", // nearest integer rounding logic
                        0, // rounding to nearest n
                        0, // monthly repayment frequency day
                        8, // service charge rate precision
                        2, // principal amount precision
                        2 // installment amount precision
                )
        );
    }



    @ParameterizedTest
    @MethodSource("repaymentScheduleCommandProvider")
    void viewRepaymentScheduleFlatWhenInstallmentInfoProvided(RepaymentScheduleCommand command) {
        *//*ModelMapper modelMapper = new ModelMapper();

        Mono<List<RepaymentScheduleResponseDTO>> repaymentSchedule = service.generateRepaymentScheduleFlatInstallmentAmountProvidedForMigrationV2(command);

        repaymentSchedule.flatMapMany(Flux::fromIterable).map(repaymentScheduleResponseDTO -> modelMapper.map(repaymentScheduleResponseDTO, RepaymentScheduleViewDTO.class))
                .collectList()
                .doOnNext(this::printRepaymentScheduleWithDates)
                .subscribe();*//*
    }



    private static Stream<? extends Arguments> repaymentScheduleCommandProvider() {
        return Stream.of(
                Arguments.of(
                        RepaymentScheduleCommand
                                .builder()
                                .loanAmount(BigDecimal.valueOf(10000))
                                .totalServiceCharge(BigDecimal.valueOf(1270))
                                .installmentAmount(BigDecimal.valueOf(250))
                                .installmentPrincipal(BigDecimal.valueOf(222))
                                .installmentServiceCharge(BigDecimal.valueOf(28))
                                .totalOutstandingAmount(BigDecimal.valueOf(6520))
                                .outstandingPrincipal(BigDecimal.valueOf(5782))
                                .outstandingServiceCharge(BigDecimal.valueOf(738))
                                .cutOffDate(LocalDate.of(2024, 6,30))
                                .repaymentFrequency("Weekly")
                                .graceDays(0)
                                .build()
                )
        );
    }

    private void printRepaymentScheduleWithDates(List<RepaymentScheduleViewDTO> repaymentScheduleDtoList) {

        System.out.println("|------|------------------|-----------|--------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");
        System.out.println("|  No. |       Date       |    Day    | Beginning Balance  |  Scheduled Payment |  Extra Payment |  Total Payment | Principal |  Interest  |  Ending Balance |");
        System.out.println("|------|------------------|-----------|--------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");
        repaymentScheduleDtoList.stream().skip(1)
                .forEach(item -> System.out.println(
                        "|" + prettyPrintWithStringWithEqualLength(refactorWholeNumbers(item.getInstallNo()), 5) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().toString()), 17) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().getDayOfWeek().toString()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getBeginPrinBalance()), 19) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getScheduledPayment()), 19) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getExtraPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getTotalPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getPrincipal()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getServiceCharge()), 11) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getEndPrinBalance()), 16) + " |"));

        System.out.println("|______|__________________|___________|____________________|____________________|________________|________________|___________|____________|_________________|");
        BigDecimal totalPrincipal = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleViewDTO::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterest = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleViewDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaymentDue = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleViewDTO::getTotalPayment).reduce(BigDecimal.ZERO, BigDecimal::add);


        System.out.println();
        printWithTwoDecimalPlaces(Math.round(totalPrincipal.doubleValue()), "Total Principal");
        printWithTwoDecimalPlaces(totalInterest, "Total Interest");
        printWithTwoDecimalPlaces(totalPaymentDue, "Total Payment Due");
    }



    private void printFlatRepaymentSchedule(List<RepaymentScheduleViewDTO> repaymentScheduleDtoList) {

        System.out.println("|------|------------------|-----------|--------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");
        System.out.println("|  No. |       Date       |    Day    | Beginning Balance  |  Scheduled Payment |  Extra Payment |  Total Payment | Principal |  Interest  |  Ending Balance |");
        System.out.println("|------|------------------|-----------|--------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");
        repaymentScheduleDtoList.stream()
                .forEach(item -> System.out.println(
                        "|" + prettyPrintWithStringWithEqualLength(refactorWholeNumbers(item.getInstallNo()), 5) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().toString()), 17) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().getDayOfWeek().toString()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getBeginPrinBalance()), 19) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getScheduledPayment()), 19) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getExtraPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getTotalPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getPrincipal()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getServiceCharge()), 11) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getEndPrinBalance()), 16) + " |"));

        System.out.println("|______|__________________|___________|____________________|____________________|________________|________________|___________|____________|_________________|");
        BigDecimal totalPrincipal = repaymentScheduleDtoList.stream().map(RepaymentScheduleViewDTO::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterest = repaymentScheduleDtoList.stream().map(RepaymentScheduleViewDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaymentDue = repaymentScheduleDtoList.stream().map(RepaymentScheduleViewDTO::getTotalPayment).reduce(BigDecimal.ZERO, BigDecimal::add);


        System.out.println();
        printWithTwoDecimalPlaces(Math.round(totalPrincipal.doubleValue()), "Total Principal");
        printWithTwoDecimalPlaces(totalInterest, "Total Interest");
        printWithTwoDecimalPlaces(totalPaymentDue, "Total Payment Due");
    }



    *//*
    * BigDecimal loanAmount, BigDecimal serviceChargeRate, String serviceChargeRateFrequency, Integer noOfInstallments, String repaymentFrequency, Integer graceDays, LocalDate disburseDate, String samityDay, String roundingLogic, Integer roundingToNearest, Integer monthlyRepaymentFrequencyDay
    * */
}