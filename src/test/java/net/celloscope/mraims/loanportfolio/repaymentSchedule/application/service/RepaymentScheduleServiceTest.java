package net.celloscope.mraims.loanportfolio.repaymentSchedule.application.service;

import net.celloscope.mraims.loanportfolio.core.util.enums.RepaymentScheduleEnum;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponseDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleViewDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.out.RepaymentSchedulePersistencePort;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.service.LoanRepaymentScheduleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
@SpringBootTest
public class RepaymentScheduleServiceTest {

    /*private RepaymentSchedulePersistencePort port = null;
    private ModelMapper modelMapper = null;*/
    @Autowired
    private LoanRepaymentScheduleService repaymentScheduleService;


    RepaymentSchedulePersistencePort repaymentSchedulePersistencePort;

    private static Stream<? extends Arguments> testParamProvider() {
        return Stream.of(
                Arguments.of(BigDecimal.valueOf(30000),
                        BigDecimal.valueOf(0.24),
                        "Yearly",
                        45,
                        null,
                        null,
                        BigDecimal.valueOf(750),
                        15,
                        LocalDate.parse("01-01-2023", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                        "SUNDAY",
                        "12",
                        "HalfUp",
                        "loanAccountId",
                        "memberId",
                        "mfiId",
                        "ACTIVE"
                ));
    }

    /* private RepaymentScheduleService repaymentScheduleService = new RepaymentScheduleService(port, modelMapper);*/

    /*RepaymentScheduleServiceTest(RepaymentSchedulePersistencePort port, ModelMapper modelMapper, RepaymentScheduleService repaymentScheduleService) {
        this.port = port;
        this.modelMapper = modelMapper;
        this.repaymentScheduleService = repaymentScheduleService;
    }

    @Test
    void contextLoading() {
        Assertions.assertNotNull(repaymentScheduleService);
        Assertions.assertNotNull(RepaymentScheduleService.class);
    }*/

    public static String refactorWithTwoDecimalPlaces(Number number) {
        DecimalFormat decimalFormat = new DecimalFormat("00.00");
        return decimalFormat.format(number);
    }

    private static String refactorWithFourDecimalPlaces(Number number) {
        DecimalFormat decimalFormat = new DecimalFormat("00.0000");
        return decimalFormat.format(number);
    }

/*    private static Stream<? extends Arguments> testParamDTOProvider() {
        return Stream.of(
                Arguments.of(RepaymentScheduleTestParamDTO
                        .builder()
                                .principal(BigDecimal.valueOf(30000))
                                .serviceChargeRate(BigDecimal.valueOf(0.24))
                                .serviceChargeRateFrequency("Yearly")
                                .noOfInstallments(45)
                                .paymentPeriod(null)
                                .daysInYear(null)
                                .installmentAmount(BigDecimal.valueOf(750))
                                .graceDays(15)
                                .disburseDate(LocalDate.parse("01-01-2023", DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                                .samityDay("SUNDAY")
                                .loanTerm("12")
                                .roundingLogic("HalfUp")
                        .build())
        );
    }*/

    public static String refactorWholeNumbers(Number number) {
        DecimalFormat decimalFormat = new DecimalFormat("00");
        return decimalFormat.format(number);
    }

    public static String prettyPrintWithStringWithEqualLength(String number, Integer maximumLength) {
        return String.format("%1$" + maximumLength + "s", number);
    }

    public static void printWithTwoDecimalPlaces(Number number, String string) {
        DecimalFormat decimalFormat = new DecimalFormat("00.00");
        System.out.println(string + " = " + decimalFormat.format(number));
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
    /*void getRepaymentSchedule_whenPaymentPeriod_Defined() throws ExceptionHandlerUtil {
        BigDecimal principal = BigDecimal.valueOf(1000);
        BigDecimal serviceChargeRate = BigDecimal.valueOf(0.25);
        String serviceChargeRateFrequency = "Yearly";                   // Yearly, Monthly
        Integer noOfInstallments = 50;
        String paymentPeriod = "week";                                  // Week, Month, Half-year
        String daysInYear = "365";
        BigDecimal installmentAmount = BigDecimal.valueOf(22.00);
        Integer graceDays = 0;
        LocalDate disburseDate = LocalDate.parse("01-01-2023", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String samityDay = "SUNDAY";
        String loanTerm = "1 Year";
        String roundingLogic = "HalfUp";                                // rounding logic for decimals. -> HalfUp, HalfDown, Up, Down
        Integer monthlyRepaymentFrequencyDay = 10;

        Mono<List<RepaymentScheduleResponseDTO>> actualResponse = repaymentScheduleService.getRepaymentScheduleForLoan(principal, serviceChargeRate, serviceChargeRateFrequency, noOfInstallments, installmentAmount, graceDays, disburseDate, samityDay, loanTerm, paymentPeriod, roundingLogic, "loanAccountId", "memberID", "mfiId", "pending", "createdBy", monthlyRepaymentFrequencyDay, RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE.getValue());
        printRepaymentScheduleWithDates(actualResponse.block());

    }*/

//    @ParameterizedTest
//    @MethodSource("testParamProvider")
    /*void getRepaymentSchedule_whenPaymentPeriod_NotDefined(BigDecimal principal, BigDecimal serviceChargeRate, String serviceChargeRateFrequency, Integer noOfInstallments, BigDecimal installmentAmount, Integer graceDays, LocalDate disburseDate, String samityDay, String loanTerm, String paymentPeriod, String roundingLogic, String loanAccountId, String memberId, String mfiId, String status) {
        Mono<List<RepaymentScheduleResponseDTO>> actualResponse = repaymentScheduleService.getRepaymentScheduleForLoan(principal, serviceChargeRate, serviceChargeRateFrequency, noOfInstallments, installmentAmount, graceDays, disburseDate, samityDay, loanTerm, paymentPeriod, roundingLogic, loanAccountId, memberId, mfiId, status,"createdBy", 10, RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE.getValue());
        printRepaymentScheduleWithoutDates(actualResponse.block());

    }*/

    @Test
    void getRepaymentScheduleWithFlatPrincipal() {
        BigDecimal loanAmount = BigDecimal.valueOf(1000);
        BigDecimal serviceChargeRate = BigDecimal.valueOf(25);
        String serviceChargeRateFrequency = "Yearly";                   // Yearly, Monthly
        Integer noOfInstallments = 50;
        Integer graceDays = 15;
        LocalDate disburseDate = LocalDate.parse("01-01-2023", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String samityDay = "SUNDAY";
        String loanTerm = "1 Year";
        String paymentPeriod = "Weekly";                                  // Week, Month, Half-year
        String roundingLogic = "HalfUp";                                // rounding logic for decimals. -> HalfUp, HalfDown, Up, Down
        String daysInYear = "365";                                      // set null to calculate SC with 1 Year = 52 Weeks
        Integer serviceChargeRatePrecision = 9;                         // round to n decimal
        Integer serviceChargePrecision = 4;                             // round to n decimal
        Integer installmentAmountPrecision = 2;                         // round to n decimal
        String installmentRoundingTo = null;                            // round installment amount. null -> not rounded; "1" -> natural Rounding using Math.round()
        Integer monthlyRepaymentFrequencyDay = 10;

        Mono<List<RepaymentScheduleViewDTO>> actualResponse = repaymentScheduleService.getRepaymentScheduleWithFlatPrincipal(loanAmount, serviceChargeRate, serviceChargeRateFrequency, noOfInstallments, graceDays, disburseDate, samityDay, loanTerm, paymentPeriod, roundingLogic, daysInYear, serviceChargeRatePrecision, serviceChargePrecision, installmentAmountPrecision, installmentRoundingTo, monthlyRepaymentFrequencyDay);
        printRepaymentScheduleFlatPrincipal(actualResponse.block());

    }

    private void printRepaymentScheduleWithDates(List<RepaymentScheduleResponseDTO> repaymentScheduleDtoList) {

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
        BigDecimal totalPrincipal = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleResponseDTO::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterest = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleResponseDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaymentDue = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleResponseDTO::getTotalPayment).reduce(BigDecimal.ZERO, BigDecimal::add);


        System.out.println();
        printWithTwoDecimalPlaces(Math.round(totalPrincipal.doubleValue()), "Total Principal");
        printWithTwoDecimalPlaces(totalInterest, "Total Interest");
        printWithTwoDecimalPlaces(totalPaymentDue, "Total Payment Due");
    }

    private void printRepaymentScheduleWithoutDates(List<RepaymentScheduleResponseDTO> repaymentScheduleDtoList) {
        System.out.println("|-------------|---------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");
        System.out.println("| Payment No. |  Beginning Balance  |  Scheduled Payment |  Extra Payment |  Total Payment | Principal |  Interest  |  Ending Balance |");
        System.out.println("|-------------|---------------------|--------------------|----------------|----------------|-----------|------------|-----------------|");

        repaymentScheduleDtoList.stream().skip(1)
                .forEach(item -> System.out.println(
                        "|" + prettyPrintWithStringWithEqualLength(refactorWholeNumbers(item.getInstallNo()), 12) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getBeginPrinBalance()), 20) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getScheduledPayment()), 19) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getExtraPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getTotalPayment()), 15) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getPrincipal()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getServiceCharge()), 11) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getEndPrinBalance()), 16) + " |"));

        System.out.println("|_____________|_____________________|____________________|________________|________________|___________|____________|_________________|");
        BigDecimal totalPrincipal = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleResponseDTO::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterest = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleResponseDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaymentDue = repaymentScheduleDtoList.stream().skip(1).map(RepaymentScheduleResponseDTO::getTotalPayment).reduce(BigDecimal.ZERO, BigDecimal::add);


        System.out.println();
        printWithTwoDecimalPlaces(Math.round(totalPrincipal.doubleValue()), "Total Principal");
        printWithTwoDecimalPlaces(totalInterest, "Total Interest");
        printWithTwoDecimalPlaces(totalPaymentDue, "Total Payment Due");
    }

    private void printRepaymentScheduleFlatPrincipal(List<RepaymentScheduleViewDTO> repaymentSchedulesDtoList) {
        System.out.println("|------|------------------|-----------|------------------------|------------|-----------|---------------------|");
        System.out.println("|  No. |       Date       |    Day    |  Outstanding_Principal |  Principal |  Interest |  Installment Amount |");
        System.out.println("|------|------------------|-----------|------------------------|------------|-----------|---------------------|");
        repaymentSchedulesDtoList.forEach(item ->
                System.out.println(
                        "|" + prettyPrintWithStringWithEqualLength(refactorWholeNumbers(item.getInstallNo()), 5) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().toString()), 17) +
                                " |" + prettyPrintWithStringWithEqualLength((item.getInstallDate().getDayOfWeek().toString()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getEndPrinBalance()), 23) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getPrincipal()), 11) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithFourDecimalPlaces(item.getServiceCharge()), 10) +
                                " |" + prettyPrintWithStringWithEqualLength(refactorWithTwoDecimalPlaces(item.getTotalPayment()), 20) + " |"));
        System.out.println("|------|------------------|-----------|------------------------|------------|-----------|---------------------|");
        /*System.out.println("|------|------------------|-----------|------------------------|------------------|------------|-----------|---------------------|");*/
        BigDecimal totalPrincipal = repaymentSchedulesDtoList.stream().map(RepaymentScheduleViewDTO::getPrincipal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalInterest = repaymentSchedulesDtoList.stream().map(RepaymentScheduleViewDTO::getServiceCharge).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPaid = repaymentSchedulesDtoList.stream().map(RepaymentScheduleViewDTO::getTotalPayment).reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println();
        printWithTwoDecimalPlaces(Math.round(totalPrincipal.doubleValue()), "TOTAL PRINCIPAL");
        printWithTwoDecimalPlaces(totalInterest, "TOTAL INTEREST");
        printWithTwoDecimalPlaces(totalPaid, "TOTAL PAID");
    }
}