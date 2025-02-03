package net.celloscope.mraims.loanportfolio.features.fdr.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.FDRClosureCommand;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.FDRClosureDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.FDRAccountDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.HolidayUseCase;
import net.celloscope.mraims.loanportfolio.features.calendar.application.port.in.dto.response.HolidayResponseDTO;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.FDRInterestCalculationEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.FDRRequestDTO;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.CalculationMetaProperty;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuples;

@ExtendWith(MockitoExtension.class)
class FDRServiceTest {

    @InjectMocks
    private FDRService service;
    @Mock
    private HolidayUseCase holidayUseCase;
    @Mock
    private CommonRepository commonRepository;
    @Mock
    private MetaPropertyUseCase metaPropertyUseCase;
    @Mock
    private PassbookUseCase passbookUseCase;
    @Mock
    private ISavingsAccountUseCase savingsAccountUseCase;

    /*
     * @ParameterizedTest
     * 
     * @MethodSource("scheduleGenerationArgumentsProvider")
     * void getFDRInterestPostingSchedule(List<LocalDate> holidayList,
     * FDRInterestCalculationEntity fdrInterestCalculationEntity, Tuple2<Double,
     * Integer> tupleOfInterestRatePerPostingPeriodAndDepositTermInMonths, String
     * loginId) {
     * List<FDRSchedule> fdrScheduleList =
     * service.getFDRInterestPostingSchedule(holidayList,
     * fdrInterestCalculationEntity,
     * tupleOfInterestRatePerPostingPeriodAndDepositTermInMonths, loginId,
     * fdrInterestCalculationEntity.getAcctStartDate());
     * System.out.println(fdrScheduleList);
     * }
     */

    private static Stream<? extends Arguments> scheduleGenerationArgumentsProvider() {
        return Stream.of(
                Arguments.of(
                        List.of(LocalDate.of(2023, 4, 10), LocalDate.of(2023, 5, 10)),
                        FDRInterestCalculationEntity
                                .builder()
                                .savingsAccountId("savings 101")
                                .savingsAmount(BigDecimal.valueOf(5000))
                                .interestRate(10.0)
                                .interestRateFrequency("Monthly")
                                .interestPostingPeriod("Monthly")
                                .depositTerm(12)
                                .depositTermPeriod("Year")
                                .acctStartDate(LocalDate.of(2023, 2, 11))
                                .build(),
                        Tuples.of(0.00833333, 12),
                        "mfi-admin"));
    }

    @Test
    void contextLoading() {
        Assertions.assertNotNull(service);
        Assertions.assertNotNull(FDRService.class);
    }

    @ParameterizedTest()
    @MethodSource("fdrEncashmentCommandProvider")
    void testEncashFDRAccount(FDRClosureCommand command) {

        Mockito
                .doReturn(Mono.just(PassbookResponseDTO
                        .builder()
                                .savgAcctEndingBalance(BigDecimal.valueOf(5100))
                                .transactionDate(LocalDate.of(2023, 6, 1))
                                .totalAccruedInterDeposit(BigDecimal.valueOf(100))
                        .build()))
                .when(passbookUseCase).getLastPassbookEntryBySavingsAccount(Mockito.any());

        Mockito
                .doReturn(Mono.just(FDRAccountDTO
                        .builder()
                                .savingsAccountId("SA0361")
                                .acctStartDate(LocalDate.of(2023, 1, 1))
                                .acctEndDate(LocalDate.of(2024, 1, 1))
                                .savingsAmount(BigDecimal.valueOf(5000))
                                .interestRate(BigDecimal.valueOf(9))
                        .build()))
                .when(savingsAccountUseCase).getFDRAccountDetailsBySavingsAccountId(Mockito.any());

        Mono<FDRClosureDTO> actualResponse = service.closeFDRAccount(command);

        StepVerifier
                .create(actualResponse)
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }


    private static Stream<? extends Arguments> fdrEncashmentCommandProvider() {
        return Stream.of(
                Arguments.of(
                        FDRClosureCommand
                                .builder()
                                .savingsAccountId("SA0361")
                                .encashmentDate(LocalDate.of(2024,1,2))
//                                .encashmentDate(LocalDate.of(2023,12,31))
                                .paymentMode("Cash")
                                .effectiveInterestRate(BigDecimal.valueOf(5))
                                .build()));
    }


    /*
     * @ParameterizedTest
     * 
     * @MethodSource("requestDTOProvider")
     * void generateFDRInterestPostingSchedule(FDRRequestDTO requestDTO) {
     * mockGetHolidays();
     * mockGetFDRInterestCalculationEntity();
     * mockGetMetaProperty();
     * Mono<FDRResponseDTO> actualResponse =
     * service.getFDRSchedule(Mockito.any()).log();
     * 
     * StepVerifier
     * .create(actualResponse)
     * .expectSubscription()
     * .expectNextCount(1)
     * .verifyComplete();
     * }
     */

    private static Stream<? extends Arguments> requestDTOProvider() {
        return Stream.of(
                Arguments.of(
                        FDRRequestDTO
                                .builder()
                                .savingsAccountId("savings 101")
                                .loginId("mfi-admin")
                                .build()));
    }



    private void mockGetHolidays() {
        Mockito.doReturn(Flux.just(
                HolidayResponseDTO
                        .builder()
                        .holidayDate(LocalDate.of(2023, 4, 11))
                        .build(),
                HolidayResponseDTO
                        .builder()
                        .holidayDate(LocalDate.of(2023, 5, 11))
                        .build()))
                .when(holidayUseCase).getAllHolidaysOfASamityBySavingsAccountId(Mockito.any());
    }

    private void mockGetFDRInterestCalculationEntity() {
        Mockito
                .doReturn(Mono.just(
                        FDRInterestCalculationEntity
                                .builder()
                                .savingsAccountId("savings 101")
                                .savingsAmount(BigDecimal.valueOf(1000))
                                .interestRate(10.0)
                                .interestRateFrequency("Yearly")
                                .interestPostingPeriod("Monthly")
                                .depositTerm(12)
                                .depositTermPeriod("Month")
                                .acctStartDate(LocalDate.of(2023, 2, 11))
                                .build()))
                .when(commonRepository).getFDRInterestCalculationEntityBySavingsAccountId(Mockito.any());
    }

    /*private void mockGetMetaProperty() {
        Mockito
                .doReturn(Mono.just(
                        CalculationMetaProperty
                                .builder()
                                .interestRatePrecision(8)
                                .roundingLogic("HalfUp")
                                .build()))
                .when(metaPropertyUseCase).getCalculationMetaProperty();
    }*/
}