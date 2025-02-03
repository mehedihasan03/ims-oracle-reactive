package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.out.gateway.entity.StagingAccountDataEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.*;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RebateInfoEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.entity.RepaymentScheduleEntity;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.out.persistence.database.repository.RepaymentScheduleRepository;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.domain.RepaymentSchedule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.reactivestreams.Publisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testng.asserts.Assertion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
@Slf4j
@ExtendWith(MockitoExtension.class)
class RepaymentScheduleServiceRefactoredTest {

  /*  @InjectMocks
    RepaymentScheduleServiceRefactored serviceRefactored;

    @Mock
    CommonRepository commonRepository;
    @Mock
    RepaymentSchedulePersistencePort port;

    @Test
    void testGetRepaymentSchedule() {

        BigDecimal principal = BigDecimal.valueOf(1000);
        BigDecimal serviceChargeRate = BigDecimal.valueOf(0.25);
        String serviceChargeRateFrequency = "Yearly";                   // Yearly, Monthly
        Integer noOfInstallments = 5;
        String paymentPeriod = "week";                                  // Week, Month, Half-year
        String daysInYear = "365";
        BigDecimal installmentAmount = BigDecimal.valueOf(230);
        Integer graceDays = 15;
        LocalDate disburseDate = LocalDate.parse("01-08-2023", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String samityDay = "SUNDAY";
        String loanTerm = "1 Year";
        String roundingLogic = "HalfUp";                                // rounding logic for decimals. -> HalfUp, HalfDown, Up, Down

        Mockito.doReturn(Flux.just(HolidayEntity.builder().holidayDate(LocalDate.of(2023, 8, 20)).build() , HolidayEntity.builder().holidayDate(LocalDate.of(2023, 9, 3)).build()))
                .when(commonRepository).getAllHolidaysOfASamityByLoanAccountId(Mockito.any());

        Mono<List<RepaymentScheduleResponseDTO>> actualResponse = serviceRefactored.getRepaymentSchedule(principal, serviceChargeRate, serviceChargeRateFrequency, noOfInstallments, installmentAmount, graceDays, disburseDate, samityDay, loanTerm, paymentPeriod, roundingLogic, "loanAccountId", "memberID", "mfiId", "pending", "createdBy").log();

        StepVerifier
                .create(actualResponse)
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();

    }





    *//*@ParameterizedTest
    @MethodSource("repaymentDatesArgumentProvider")*//*
    void getRepaymentDates(LocalDate disburseDate, DayOfWeek samityDay, Integer graceDays, Integer noOfInstallments, String paymentPeriod) {
        *//*Set<LocalDate> actualResponse = repaymentScheduleServiceRefactored.getRepaymentDates(disburseDate, samityDay, graceDays, noOfInstallments, paymentPeriod);
        Assertions.assertThat(actualResponse.size()).isEqualTo(5);*//*
    }

    private static Stream<? extends Arguments> repaymentDatesArgumentProvider() {
        return Stream.of(
                Arguments.of(LocalDate.of(2023,8,1),
                        DayOfWeek.SUNDAY,
                        15,
                        5,
                        "WEEk"));
    }*/
}