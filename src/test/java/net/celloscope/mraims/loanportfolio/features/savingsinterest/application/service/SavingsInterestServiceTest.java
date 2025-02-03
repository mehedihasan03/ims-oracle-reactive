package net.celloscope.mraims.loanportfolio.features.savingsinterest.application.service;

import net.celloscope.mraims.loanportfolio.core.util.enums.SavingsBalanceCalculationMethods;
import net.celloscope.mraims.loanportfolio.features.common.queries.entities.SavingsAccountProductEntity;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.MetaPropertyUseCase;
import net.celloscope.mraims.loanportfolio.features.metaproperty.application.port.in.response.MetaPropertyResponseDTO;
import net.celloscope.mraims.loanportfolio.features.passbook.application.port.in.PassbookUseCase;
import net.celloscope.mraims.loanportfolio.features.passbook.application.service.helpers.dto.PassbookResponseDTO;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.CalculateInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.AccruedInterestDTODomain;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.CalculateInterestData;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.domain.ISavingsInterestCommands;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class SavingsInterestServiceTest {

    @InjectMocks
    private SavingsInterestService service;
    @Mock
    private ISavingsInterestCommands savingsInterestCommands;
    @Mock
    private PassbookUseCase passbookUseCase;
    @Mock
    private CommonRepository commonRepository;
    @Mock
    private MetaPropertyUseCase metaPropertyUseCase;

/*    @ParameterizedTest
    @MethodSource("commandProvider")
    void calculateInterest(CalculateInterestCommand command) {
        mockGetSavingsProductEntityBySavingsAccountId();
        mockGetMetaProperty();
        mockGetPassbookEntriesBySavingsAccountIDAndTransactionDateOrderByCreatedOn();
        mockCalculateDailyAccruedInterest();
        Mono<SavingsInterestResponseDTO> actualResponse = service.calculateDailyAccruedInterest(command).log();

        StepVerifier
                .create(actualResponse)
                .expectSubscription()
                .expectNextCount(1)
                .verifyComplete();
    }*/

    private static Stream<? extends Arguments> commandProvider(){
        return Stream.of(
                Arguments.of(CalculateInterestCommand
                        .builder()
                        .savingsAccountId("savingsAccountId")
//                        .interestRate(BigDecimal.valueOf(10))
                        .interestCalculationDate(LocalDate.now())
                        /*.interestRateFrequency("MONTHLY")
                        .interestRatePrecision(8)
                        .accruedInterestPrecision(4)
                        .roundingMode(RoundingMode.HALF_UP)
                        .daysInYear("365")
                        .balanceCalculationMethod(SavingsBalanceCalculationMethods.AVERAGE_DAILY_BALANCE.getValue())*/
                        .build())
        );
    }

    @ParameterizedTest
    @MethodSource("monthlyAccruedInterestProvider")
    void givenMonthYearPassbookListAndCalculateInterestData_whenGetMonthlyAccruedInterest_thenReturnTotalMonthlyAccruedInterest(Integer interestCalculationMonth, Integer interestCalculationYear, List<PassbookResponseDTO> passbookResponseDTOList, CalculateInterestData data) {

        Mockito
                .doReturn(BigDecimal.valueOf(10))
                .when(savingsInterestCommands).calculateDailyAccruedInterest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());

        AccruedInterestDTODomain accruedMonthlyInterest = service.getMonthlyAccruedInterest(interestCalculationMonth, interestCalculationYear, passbookResponseDTOList, data);
        System.out.println("from test : " +accruedMonthlyInterest);
    }


    @ParameterizedTest
    @MethodSource("calculateAvailableBalanceProvider")
    void getSavingsBalanceAccordingToCalculationMethod(List<PassbookResponseDTO> passbookList, String balanceCalculationMethod, RoundingMode roundingMode) {
        Mono<BigDecimal> actualResponse = service.getSavingsBalanceMonoAccordingToCalculationMethod(passbookList, balanceCalculationMethod, roundingMode).log();

        StepVerifier
                .create(actualResponse)
                .expectSubscription()
                .expectNext(BigDecimal.valueOf(35))
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("monthlyAccruedInterestProvider")
    void getMonthlyAccruedInterestV2Test(Integer interestCalculationMonth, Integer interestCalculationYear, List<PassbookResponseDTO> passbookResponseDTOList, CalculateInterestData data) {
        /*AccruedInterestDTODomain actualResponse = service.getMonthlyAccruedInterestForGsVs(interestCalculationMonth, interestCalculationYear, passbookResponseDTOList, data);

        System.out.println("accrued interest : " + actualResponse);*/
    }




    private static Stream<? extends Arguments> monthlyAccruedInterestProvider(){
        return Stream.of(
                Arguments.of(
                        1,
                        2023,
                        List.of(PassbookResponseDTO.builder()
                                        .transactionDate(LocalDate.of(2022,12,20))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(900))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(1500))
                                        .build())
                        /*List.of(PassbookResponseDTO.builder()
                                        .transactionDate(LocalDate.of(2023,1,1))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(900))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(1500))
                                        .build(),
                                PassbookResponseDTO.builder()
                                        .transactionDate(LocalDate.of(2023,1,1))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(1500))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(1800))
                                        .build(),
                                PassbookResponseDTO.builder()
                                        .transactionDate(LocalDate.of(2023,1,15))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(1800))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(2000))
                                        .build(),
                                PassbookResponseDTO
                                        .builder()
                                        .transactionDate(LocalDate.of(2023,1,20))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(2000))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(2500))
                                        .build(),
                                PassbookResponseDTO
                                        .builder()
                                        .transactionDate(LocalDate.of(2023,1,31))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(2500))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(3000))
                                        .build()


                                *//*PassbookResponseDTO.builder()
                                        .transactionDate(LocalDate.of(2023,1,15))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(900))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(1500))
                                        .build()*//*
                        )*/

                        ,
                        CalculateInterestData
                                .builder()
                                .acctStartDate(LocalDate.of(2022, 12, 7))
                                .interestCalculationMonth(1)
                                .interestCalculationYear(2023)
                                .balanceCalculationMethod(SavingsBalanceCalculationMethods.END_OF_DAY_BALANCE.getValue())
                                .balanceRequiredInterestCalc(BigDecimal.valueOf(500))
                                .interestRate(BigDecimal.valueOf(10))
                                .accruedInterestPrecision(2)
                                .daysInYear("365")
                                .interestRatePrecision(8)
                                .interestRateFrequency("YEARLY")
                                .roundingMode(RoundingMode.HALF_UP)
                                .build()
                )
        );
    }


    @ParameterizedTest
    @MethodSource("monthlyAccruedInterestProviderV2")
    void getMonthlyAccruedInterestV2TestV2(Integer interestCalculationMonth, Integer interestCalculationYear, List<PassbookResponseDTO> passbookResponseDTOList, CalculateInterestData data) {
/*        AccruedInterestDTODomain actualResponse = service.getMonthlyAccruedInterestForGsVs(interestCalculationMonth, interestCalculationYear, passbookResponseDTOList, data);

        System.out.println("accrued interest : " + actualResponse);*/
    }


    private static Stream<? extends Arguments> monthlyAccruedInterestProviderV2(){
        return Stream.of(
                Arguments.of(
                        1,
                        2023,
                        List.of(PassbookResponseDTO.builder()
                                        .transactionDate(LocalDate.of(2023,1,5))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(0))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(500))
                                        .build(),
                                PassbookResponseDTO.builder()
                                        .transactionDate(LocalDate.of(2023,1,15))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(500))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(2000))
                                        .build(),
                                PassbookResponseDTO.builder()
                                        .transactionDate(LocalDate.of(2023,1,15))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(2000))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(3000))
                                        .build(),
                                PassbookResponseDTO
                                        .builder()
                                        .transactionDate(LocalDate.of(2023,1,20))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(3000))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(4000))
                                        .build(),
                                PassbookResponseDTO
                                        .builder()
                                        .transactionDate(LocalDate.of(2023,1,31))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(4000))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(4500))
                                        .build()


                                /*PassbookResponseDTO.builder()
                                        .transactionDate(LocalDate.of(2023,1,15))
                                        .savgAcctBeginBalance(BigDecimal.valueOf(900))
                                        .savgAcctEndingBalance(BigDecimal.valueOf(1500))
                                        .build()*/
                        ),
                        CalculateInterestData
                                .builder()
                                .acctStartDate(LocalDate.of(2023, 1, 5))
                                .interestCalculationMonth(1)
                                .interestCalculationYear(2023)
                                .balanceCalculationMethod(SavingsBalanceCalculationMethods.MINIMUM_DAILY_BALANCE.getValue())
                                .balanceRequiredInterestCalc(BigDecimal.valueOf(300))
                                .interestRate(BigDecimal.valueOf(10))
                                .accruedInterestPrecision(2)
                                .daysInYear("365")
                                .interestRatePrecision(8)
                                .interestRateFrequency("YEARLY")
                                .roundingMode(RoundingMode.HALF_UP)
                                .build()
                )
        );
    }

    private static Stream<? extends Arguments> calculateAvailableBalanceProvider(){
        return Stream.of(
                Arguments.of(
                        List.of(PassbookResponseDTO
                                        .builder()
                                        .savgAcctEndingBalance(BigDecimal.valueOf(60))
                                        .transactionDate(LocalDate.of(2023,1,1))
                                        .build(),
                                PassbookResponseDTO
                                        .builder()
                                        .savgAcctEndingBalance(BigDecimal.valueOf(40))
                                        .transactionDate(LocalDate.of(2023,1,1))
                                        .build(),
                                PassbookResponseDTO
                                        .builder()
                                        .savgAcctEndingBalance(BigDecimal.valueOf(35))
                                        .transactionDate(LocalDate.of(2023,1,1))
                                        .build()),
                        SavingsBalanceCalculationMethods.MINIMUM_DAILY_BALANCE.getValue(),
                        RoundingMode.HALF_UP
                )
        );
    }

    @ParameterizedTest
    @MethodSource("dayDifferenceParamProvider")
    void getNumberOfDaysInBetweenTest(LocalDate currentDate, LocalDate nextDate, Integer daysInCurrentMonth) {
        Long numberOfDays = service.getNumberOfDaysInBetween(currentDate, nextDate, daysInCurrentMonth);
        System.out.println(numberOfDays);
    }

    private static Stream<? extends Arguments> dayDifferenceParamProvider() {
        return Stream.of(
                Arguments.of(
                        LocalDate.of(2023,1,1),
                        LocalDate.of(2023,1,2),
                        31
                )
        );
    }

    private void mockGetLastPassbookEntryForSavings(){
        Mockito
                .doReturn(Mono.just(PassbookResponseDTO.builder().savingsAvailableBalance(BigDecimal.valueOf(1000)).build()))
                .when(passbookUseCase).getLastPassbookEntryBySavingsAccount(Mockito.any());
    }

    private void mockGetPassbookEntriesBySavingsAccountIDAndTransactionDateOrderByCreatedOn(){
        Mockito
                .doReturn(
                        Flux.just(
                                PassbookResponseDTO
                                        .builder()
                                        .savgAcctEndingBalance(BigDecimal.valueOf(40))
                                        .transactionDate(LocalDate.of(2023,1,1))
                                        .build(),
                                PassbookResponseDTO
                                        .builder()
                                        .savgAcctEndingBalance(BigDecimal.valueOf(35))
                                        .transactionDate(LocalDate.of(2023,1,1))
                                        .build(),
                                PassbookResponseDTO
                                        .builder()
                                        .savgAcctEndingBalance(BigDecimal.valueOf(60))
                                        .transactionDate(LocalDate.of(2023,1,1))
                                        .build()))
                .when(passbookUseCase).getPassbookEntriesBySavingsAccountIDAndTransactionDateOrderByCreatedOn(Mockito.any(), Mockito.any());
    }

    private void mockCalculateDailyAccruedInterest(){
        Mockito
                .doReturn(BigDecimal.valueOf(10))
                .when(savingsInterestCommands).calculateDailyAccruedInterest(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    private void mockGetSavingsProductEntityBySavingsAccountId() {
        Mockito
                .doReturn(Mono.just(
                        SavingsAccountProductEntity
                                .builder()
                                .status("ACTIVE")
                                .interestRateFrequency("MONTHLY")
                                .interestCalculatedUsing(SavingsBalanceCalculationMethods.AVERAGE_DAILY_BALANCE.getValue())
                                .interestPostingPeriod("Yearly")
                                .interestCompoundingPeriod("Yearly")
                                .balanceRequiredInterestCalc("500")
                                .build()
                ))
                .when(commonRepository).getSavingsProductEntityBySavingsAccountId(Mockito.any());
    }

    /*private void mockGetMetaProperty() {
        Mockito
                .doReturn(Mono.just(
                        MetaPropertyResponseDTO
                                .builder()
                                .parameters("{\n" +
                                        "  \"accruedInterestPrecision\": \"2\",\n" +
                                        "  \"interestRatePrecision\": \"8\",\n" +
                                        "  \"roundingLogic\": \"HalfUp\",\n" +
                                        "  \"daysInYear\": \"360\"\n" +
                                        "}")
                                .build()
                ))
                .when(metaPropertyUseCase).getMetaPropertyByDescription(Mockito.any());
    }*/


}