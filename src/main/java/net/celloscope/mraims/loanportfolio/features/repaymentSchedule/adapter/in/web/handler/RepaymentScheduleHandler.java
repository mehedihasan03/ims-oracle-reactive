package net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.enums.RepaymentScheduleEnum;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.adapter.in.web.handler.dto.out.RepaymentScheduleResponse;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.DpsRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.LoanRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.MigrationRepaymentScheduleUseCase;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.DPSRepaymentCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.LoanRepaymentScheduleRequestDTO;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.MigrationRepaymentScheduleCommand;
import net.celloscope.mraims.loanportfolio.features.repaymentSchedule.application.port.in.dto.RepaymentScheduleCommand;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class RepaymentScheduleHandler {

    private final LoanRepaymentScheduleUseCase loanRepaymentScheduleUseCase;
    private final DpsRepaymentScheduleUseCase dpsRepaymentScheduleUseCase;
    private final MigrationRepaymentScheduleUseCase migrationRepaymentScheduleUseCase;

    public Mono<ServerResponse> getRepaymentSchedule(ServerRequest serverRequest) {

        LoanRepaymentScheduleRequestDTO loanRepaymentScheduleRequestDTO =
                LoanRepaymentScheduleRequestDTO
                .builder()
                .loanAmount(new BigDecimal(serverRequest.queryParam("loanAmount").orElse("")))
                .serviceChargeRate(new BigDecimal(serverRequest.queryParam("serviceChargeRate").orElse("")))
                .serviceChargeRateFrequency(serverRequest.queryParam("serviceChargeRateFrequency").orElse(""))
                .noOfInstallments(Integer.parseInt(serverRequest.queryParam("noOfInstallments").orElse("")))
                .installmentAmount(new BigDecimal(serverRequest.queryParam("installmentAmount").orElse("")))
                .graceDays(Integer.parseInt(serverRequest.queryParam("graceDays").orElse("")))
                .disburseDate(LocalDate.parse(serverRequest.queryParam("disburseDate").orElse(""), DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .samityDay(serverRequest.queryParam(QueryParams.SAMITY_DAY.getValue()).orElse(""))
                .loanTerm(Integer.valueOf(serverRequest.queryParam(QueryParams.LOAN_TERM.getValue()).orElse("")))
                .repaymentFrequency(serverRequest.queryParam(QueryParams.PAYMENT_PERIOD.getValue()).orElse(""))
                .roundingLogic(serverRequest.queryParam(QueryParams.ROUNDING_MODE.getValue()).orElse(""))
                .loanAccountId(serverRequest.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse(""))
                .memberId(serverRequest.queryParam(QueryParams.MEMBER_ID.getValue()).orElse(""))
                .mfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""))
                .status(serverRequest.queryParam(QueryParams.STATUS.getValue()).orElse(""))
                .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                .monthlyRepaymentFrequencyDay(Integer.parseInt(serverRequest.queryParam(QueryParams.MONTHLY_REPAYMENT_FREQUENCY_DAY.getValue()).orElse("")))
                .serviceChargeCalculationMethod(serverRequest.queryParam(QueryParams.SERVICE_CHARGE_CALCULATION_METHOD.getValue()).orElse(RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE.getValue()))
                .build();

        return loanRepaymentScheduleUseCase
                .getRepaymentScheduleForLoan(loanRepaymentScheduleRequestDTO)
                .flatMap(repaymentScheduleResponseDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(repaymentScheduleResponseDTO));
    }

    public Mono<ServerResponse> viewRepaymentSchedule(ServerRequest serverRequest) {
        String roundingToNearest = serverRequest.queryParam("roundingToNearest").orElse("");
        Integer roundingToNearestInteger = roundingToNearest.equalsIgnoreCase(RepaymentScheduleEnum.NO_ROUNDING_TO_INTEGER.toString()) ? 0 : Integer.parseInt(roundingToNearest);

        LoanRepaymentScheduleRequestDTO loanRepaymentScheduleRequestDTO =
                LoanRepaymentScheduleRequestDTO
                        .builder()
                        .loanAmount(new BigDecimal(serverRequest.queryParam("loanAmount").orElse("")))
                        .serviceChargeRate(new BigDecimal(serverRequest.queryParam("serviceChargeRate").orElse("")))
                        .serviceChargeRateFrequency(serverRequest.queryParam("serviceChargeRateFrequency").orElse(""))
                        .noOfInstallments(Integer.parseInt(serverRequest.queryParam("noOfInstallments").orElse("")))
                        .graceDays(Integer.parseInt(serverRequest.queryParam("graceDays").orElse("")))
                        .disburseDate(LocalDate.parse(serverRequest.queryParam("disburseDate").orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd)))
                        .samityDay(serverRequest.queryParam("samityDay").orElse(""))
                        .loanTerm(Integer.valueOf(serverRequest.queryParam(QueryParams.LOAN_TERM.getValue()).orElse("")))
                        .repaymentFrequency(serverRequest.queryParam("repaymentFrequency").orElse(""))
                        .roundingLogic(serverRequest.queryParam("roundingLogic").orElse(""))
                        .monthlyRepaymentFrequencyDay(Integer.parseInt(serverRequest.queryParam("monthlyRepaymentFrequencyDay").orElse("1")))
                        .roundingToNearest(roundingToNearestInteger)
                        .serviceChargeCalculationMethod(serverRequest.queryParam(QueryParams.SERVICE_CHARGE_CALCULATION_METHOD.getValue()).orElse(RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE.getValue()))
                        .build();

        return loanRepaymentScheduleUseCase
                .viewRepaymentScheduleForLoan(loanRepaymentScheduleRequestDTO)
                .flatMap(repaymentScheduleResponseDTO -> {
                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(RepaymentScheduleResponse
                                    .builder()
                                    .message("Repayment Schedule Generated Successfully.")
                                    .data(repaymentScheduleResponseDTO)
                                    .build());
                });
    }


    public Mono<ServerResponse> viewRepaymentScheduleFlat(ServerRequest serverRequest) {
        LoanRepaymentScheduleRequestDTO loanRepaymentScheduleRequestDTO =
                LoanRepaymentScheduleRequestDTO
                        .builder()
                        .loanAmount(new BigDecimal(serverRequest.queryParam("loanAmount").orElse("")))
                        .serviceChargeRate(new BigDecimal(serverRequest.queryParam("serviceChargeRate").orElse("")))
                        .serviceChargeRateFrequency(serverRequest.queryParam("serviceChargeRateFrequency").orElse(""))
                        .noOfInstallments(Integer.parseInt(serverRequest.queryParam("noOfInstallments").orElse("")))
                        .graceDays(Integer.parseInt(serverRequest.queryParam("graceDays").orElse("")))
                        .disburseDate(LocalDate.parse(serverRequest.queryParam("disburseDate").orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd)))
                        .samityDay(serverRequest.queryParam("samityDay").orElse(""))
                        .repaymentFrequency(serverRequest.queryParam("repaymentFrequency").orElse(""))
                        .roundingLogic(serverRequest.queryParam("roundingMode").orElse(""))
                        .roundingToNearestIntegerLogic(serverRequest.queryParam("roundingLogic").orElse(""))
                        .monthlyRepaymentFrequencyDay(Integer.parseInt(serverRequest.queryParam("monthlyRepaymentFrequencyDay").orElse("1")))
                        .serviceChargeRatePrecision(Integer.parseInt(serverRequest.queryParam("serviceChargeRatePrecision").orElse("8")))
                        .installmentPrecision(Integer.parseInt(serverRequest.queryParam("installmentAmountPrecision").orElse("2")))
                        .serviceChargeAmountPrecision(Integer.parseInt(serverRequest.queryParam("principalAmountPrecision").orElse("2")))
                        .roundingToNearest(Integer.parseInt(serverRequest.queryParam("roundingToNearest").orElse("")))
                        .officeId(serverRequest.queryParam("officeId").orElse(""))
                        .installmentAmount(new BigDecimal(Objects.requireNonNull(serverRequest.queryParam("installmentAmount").orElse(null))))
                        .build();

        return loanRepaymentScheduleUseCase
                .viewRepaymentScheduleFlat(loanRepaymentScheduleRequestDTO)
                .flatMap(repaymentScheduleResponseDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(RepaymentScheduleResponse
                                .builder()
                                .message("Repayment Schedule Generated Successfully.")
                                .data(repaymentScheduleResponseDTO)
                                .build()));
    }


    public Mono<ServerResponse> viewRepaymentScheduleFlatMigration(ServerRequest serverRequest) {
        BigDecimal loanAmount = new BigDecimal(serverRequest.queryParam("loanAmount").orElse(""));
        BigDecimal serviceChargeRate = new BigDecimal(serverRequest.queryParam("serviceChargeRate").orElse(""));
        String serviceChargeRateFrequency = serverRequest.queryParam("serviceChargeRateFrequency").orElse("");
        Integer noOfInstallments = Integer.parseInt(serverRequest.queryParam("noOfInstallments").orElse(""));
        Integer graceDays = Integer.parseInt(serverRequest.queryParam("graceDays").orElse(""));
        LocalDate disburseDate = LocalDate.parse(serverRequest.queryParam("disburseDate").orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        String samityDay = serverRequest.queryParam("samityDay").orElse("");
        String repaymentFrequency = serverRequest.queryParam("repaymentFrequency").orElse("");
        String roundingMode = serverRequest.queryParam("roundingMode").orElse(RepaymentScheduleEnum.NATURAL_ROUNDING.toString());
        Integer roundingInstallmentToNearestInteger = Integer.parseInt(serverRequest.queryParam("roundingInstallmentToNearestInteger").orElse("1"));
        String roundingInstallmentToNearestIntegerLogic = serverRequest.queryParam("roundingInstallmentToNearestIntegerLogic").orElse(RepaymentScheduleEnum.ROUNDING_UP.toString());
        Integer monthlyRepaymentFrequencyDay = Integer.parseInt(serverRequest.queryParam("monthlyRepaymentFrequencyDay").orElse("1"));
        Integer serviceChargeRatePrecision = Integer.parseInt(serverRequest.queryParam("serviceChargeRatePrecision").orElse("8"));
        Integer installmentAmountPrecision = Integer.parseInt(serverRequest.queryParam("installmentAmountPrecision").orElse("0"));
        Integer principalAmountPrecision = Integer.parseInt(serverRequest.queryParam("principalAmountPrecision").orElse("2"));
        String officeId = serverRequest.queryParam("officeId").orElse("");
        LocalDate cutOffDate = LocalDate.parse(serverRequest.queryParam(QueryParams.CUT_OFF_DATE.getValue()).orElse(LocalDate.now().toString()), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        Integer noOfPastInstallments = Integer.parseInt(serverRequest.queryParam(QueryParams.NO_OF_PAST_INSTALLMENTS.getValue()).orElse("0"));

        return migrationRepaymentScheduleUseCase
                .viewRepaymentScheduleFlat(buildMigrationRepaymentScheduleCommandFlat(officeId, loanAmount, serviceChargeRate, serviceChargeRateFrequency, noOfInstallments, repaymentFrequency, graceDays, disburseDate, samityDay, roundingMode, roundingInstallmentToNearestIntegerLogic, roundingInstallmentToNearestInteger, monthlyRepaymentFrequencyDay, serviceChargeRatePrecision, principalAmountPrecision, installmentAmountPrecision, cutOffDate, noOfPastInstallments))
                .flatMap(tuple2 -> {
                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(RepaymentScheduleResponse
                                    .builder()
                                    .message("Repayment Schedule Generated Successfully.")
                                    .data(tuple2.getT1())
                                    .build());
                });
    }


    public Mono<ServerResponse> getRepaymentScheduleWithInstallmentInfoProvided(ServerRequest serverRequest) {
        return migrationRepaymentScheduleUseCase
//                .viewRepaymentScheduleFlatInstallmentAmountProvidedForMigration(buildRepaymentScheduleCommand(serverRequest))
                .generateRepaymentScheduleDecliningInstallmentAmountProvidedForMigration(buildRepaymentScheduleCommand(serverRequest))
                .flatMap(repaymentScheduleResponseDTO -> {
                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(RepaymentScheduleResponse
                                    .builder()
                                    .message("Repayment Schedule Generated Successfully.")
                                    .data(repaymentScheduleResponseDTO)
                                    .build());
                });
    }


    private RepaymentScheduleCommand buildRepaymentScheduleCommand(ServerRequest serverRequest) {
        BigDecimal loanAmount = new BigDecimal(serverRequest.queryParam("loanAmount").orElse(""));
        BigDecimal totalServiceCharge = new BigDecimal(serverRequest.queryParam("totalServiceCharge").orElse(""));
        BigDecimal totalOutstandingAmount = new BigDecimal(serverRequest.queryParam("totalOutstandingAmount").orElse(""));
        BigDecimal outstandingPrincipal = new BigDecimal(serverRequest.queryParam("outstandingPrincipal").orElse(""));
        BigDecimal outstandingServiceCharge = new BigDecimal(serverRequest.queryParam("outstandingServiceCharge").orElse(""));
        Integer graceDays = Integer.parseInt(serverRequest.queryParam("graceDays").orElse(""));
        LocalDate cutOffDate = LocalDate.parse(serverRequest.queryParam("cutOffDate").orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        LocalDate disbursementDate = LocalDate.parse(serverRequest.queryParam("disbursementDate").orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        String repaymentFrequency = serverRequest.queryParam("repaymentFrequency").orElse("");
        BigDecimal installmentAmount = new BigDecimal((serverRequest.queryParam("installmentAmount").orElse("")));
        BigDecimal installmentPrincipal = new BigDecimal(serverRequest.queryParam("installmentPrincipal").orElse(""));
        BigDecimal installmentServiceCharge = new BigDecimal((serverRequest.queryParam("installmentServiceCharge").orElse("")));
        BigDecimal accumulatedLoanAmount = new BigDecimal((serverRequest.queryParam("accumulatedLoanAmount").orElse("")));
        BigDecimal overdueAmount = new BigDecimal((serverRequest.queryParam("overdueAmount").orElse("")));
        Integer noOfInstallments = Integer.parseInt(serverRequest.queryParam("noOfInstallments").orElse(""));
        String loanAccountId = serverRequest.queryParam("loanAccountId").orElse("");
        Integer monthlyRepaymentFrequencyDay = Integer.parseInt(serverRequest.queryParam("monthlyRepaymentFrequencyDay").orElse("1"));
        BigDecimal annualServiceChargeRate = new BigDecimal(serverRequest.queryParam("annualServiceChargeRate").orElse("12"));

        return RepaymentScheduleCommand
                .builder()
                .loanAmount(loanAmount)
                .totalServiceCharge(totalServiceCharge)
                .totalOutstandingAmount(totalOutstandingAmount)
                .outstandingPrincipal(outstandingPrincipal)
                .outstandingServiceCharge(outstandingServiceCharge)
                .graceDays(graceDays)
                .cutOffDate(cutOffDate)
                .disbursementDate(disbursementDate)
                .repaymentFrequency(repaymentFrequency)
                .installmentAmount(installmentAmount)
                .installmentPrincipal(installmentPrincipal)
                .installmentServiceCharge(installmentServiceCharge)
                .noOfInstallments(noOfInstallments)
                .loanAccountId(loanAccountId)
                .accumulatedLoanAmount(accumulatedLoanAmount)
                .overdueAmount(overdueAmount)
                .monthlyRepaymentFrequencyDay(monthlyRepaymentFrequencyDay)
                .annualServiceChargeRate(annualServiceChargeRate)
                .build();
    }




    public Mono<ServerResponse> getRepaymentScheduleWithFlatPrincipal(ServerRequest serverRequest) {

        BigDecimal loanAmount = new BigDecimal(serverRequest.queryParam("loanAmount").orElse(""));
        BigDecimal serviceChargeRate = new BigDecimal(serverRequest.queryParam("serviceChargeRate").orElse(""));
        String serviceChargeRateFrequency = String.valueOf(serverRequest.queryParam("serviceChargeRateFrequency").orElse(""));
        Integer noOfInstallments = Integer.parseInt(serverRequest.queryParam("noOfInstallments").orElse(""));
        /*BigDecimal installmentAmount = new BigDecimal(String.valueOf(serverRequest.queryParam("installmentAmount")));*/
        Integer graceDays = Integer.parseInt(serverRequest.queryParam("graceDays").orElse(""));
        LocalDate disburseDate = LocalDate.parse(serverRequest.queryParam("disburseDate").orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        String samityDay = serverRequest.queryParam("samityDay").orElse("");
        String loanTerm = serverRequest.queryParam("loanTerm").orElse("");
        String paymentPeriod = serverRequest.queryParam("paymentPeriod").orElse("");
        String roundingLogic = serverRequest.queryParam("roundingLogic").orElse("");
        String daysInYear = serverRequest.queryParam("daysInYear").orElse("365");
        Integer serviceChargeRatePrecision = Integer.parseInt(serverRequest.queryParam("serviceChargeRatePrecision").orElse("8"));
        Integer serviceChargePrecision = Integer.parseInt((serverRequest.queryParam("serviceChargePrecision").orElse("2")));
        Integer installmentAmountPrecision = Integer.parseInt((serverRequest.queryParam("installmentAmountPrecision").orElse("2")));
        String installmentRoundingTo = serverRequest.queryParam("installmentRoundingTo").orElse(null);
        Integer monthlyRepaymentFrequencyDay =  Integer.parseInt((serverRequest.queryParam("monthlyRepaymentFrequencyDay").orElse("5")));

        return loanRepaymentScheduleUseCase
                .getRepaymentScheduleWithFlatPrincipal(loanAmount, serviceChargeRate, serviceChargeRateFrequency, noOfInstallments, graceDays, disburseDate, samityDay, loanTerm, paymentPeriod, roundingLogic, daysInYear, serviceChargeRatePrecision, serviceChargePrecision, installmentAmountPrecision, installmentRoundingTo, monthlyRepaymentFrequencyDay)
                .flatMap(repaymentSchedules -> {
                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(RepaymentScheduleResponse
                                    .builder()
                                    .message("Repayment Schedule Generated Successfully.")
                                    .data(repaymentSchedules)
                                    .build());
                });
    }

    public Mono<ServerResponse> getRepaymentInfo(ServerRequest serverRequest) {
        String loanAccountId = serverRequest.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse("");
        Integer installmentNo = Integer.parseInt(serverRequest.queryParam(QueryParams.INSTALLMENT_NO.getValue()).orElse(""));

        return loanRepaymentScheduleUseCase
                .getRepaymentDetailsByInstallmentNoAndLoanAccountId(installmentNo, loanAccountId)
                .flatMap(repaymentScheduleResponseDTO -> {
                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(repaymentScheduleResponseDTO);
                });
    }

    public Mono<ServerResponse> getRepaymentScheduleByLoanAccountId(ServerRequest serverRequest) {
        String loanAccountId = serverRequest.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse("");

        return loanRepaymentScheduleUseCase
                .getRepaymentScheduleByLoanAccountId(loanAccountId)
                .flatMapMany(Flux::fromIterable)
                .map(repaymentScheduleResponseDTO -> {
                    repaymentScheduleResponseDTO.setPrincipal(repaymentScheduleResponseDTO.getPrincipal().setScale(2, RoundingMode.HALF_UP));
                    repaymentScheduleResponseDTO.setServiceCharge(repaymentScheduleResponseDTO.getServiceCharge().setScale(2, RoundingMode.HALF_UP));
                    repaymentScheduleResponseDTO.setBeginPrinBalance(repaymentScheduleResponseDTO.getBeginPrinBalance().setScale(2, RoundingMode.HALF_UP));
                    repaymentScheduleResponseDTO.setEndPrinBalance(repaymentScheduleResponseDTO.getEndPrinBalance().setScale(2, RoundingMode.HALF_UP));
                    repaymentScheduleResponseDTO.setTotalPayment(repaymentScheduleResponseDTO.getTotalPayment().setScale(2, RoundingMode.HALF_UP));
                    repaymentScheduleResponseDTO.setScheduledPayment(repaymentScheduleResponseDTO.getScheduledPayment().setScale(2, RoundingMode.HALF_UP));
                    repaymentScheduleResponseDTO.setExtraPayment(repaymentScheduleResponseDTO.getExtraPayment().setScale(2, RoundingMode.HALF_UP));
                    return repaymentScheduleResponseDTO;
                })
                .collectList()
                .flatMap(repaymentScheduleResponseDTO -> {
                    return ServerResponse
                            .ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(repaymentScheduleResponseDTO);
                });
    }

    public Mono<ServerResponse> generateRepaymentScheduleForDps(ServerRequest serverRequest) {
        String savingsAccountId = serverRequest.queryParam("savingsAccountId").orElse("");
        LocalDate firstInstallmentDate = LocalDate.parse(serverRequest.queryParam("firstInstallmentDate").orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));

        return dpsRepaymentScheduleUseCase
                .generateDpsRepaymentSchedule(DPSRepaymentCommand
                        .builder()
                        .savingsAccountId(savingsAccountId)
                        .firstInstallmentDate(firstInstallmentDate)
                        .build())
                .flatMap(dpsRepaymentScheduleResponseDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsRepaymentScheduleResponseDTO));
    }

    public Mono<ServerResponse> rescheduleLoanRepayScheduleOnSamityCancel(ServerRequest serverRequest){
        return loanRepaymentScheduleUseCase.rescheduleLoanRepayScheduleOnSamityCancel(List.of(serverRequest.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse("")), "Faisal", LocalDate.now())
                .flatMap(metaPropertyResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(metaPropertyResponseDTO));
    }


    private MigrationRepaymentScheduleCommand buildMigrationRepaymentScheduleCommandFlat(
            String officeId,
            BigDecimal loanAmount,
            BigDecimal serviceChargeRate,
            String serviceChargeRateFrequency,
            Integer noOfInstallments,
            String repaymentFrequency,
            Integer graceDays,
            LocalDate disbursementDate,
            String samityDay,
            String roundingMode,
            String roundingInstallmentToNearestIntegerLogic,
            Integer roundingInstallmentToNearestInteger,
            Integer monthlyRepaymentFrequencyDay,
            Integer serviceChargeRatePrecision,
            Integer principalAmountPrecision,
            Integer installmentAmountPrecision,
            LocalDate cutOffDate,
            Integer noOfPastInstallments
    ) {
        return MigrationRepaymentScheduleCommand
                .builder()
                .officeId(officeId)
                .loanAmount(loanAmount)
                .serviceChargeRate(serviceChargeRate)
                .serviceChargeRateFrequency(serviceChargeRateFrequency)
                .noOfInstallments(noOfInstallments)
                .repaymentFrequency(repaymentFrequency)
                .graceDays(graceDays)
                .disburseDate(disbursementDate)
                .samityDay(samityDay)
                .roundingMode(roundingMode)
                .roundingInstallmentToNearestIntegerLogic(roundingInstallmentToNearestIntegerLogic)
                .roundingInstallmentToNearestInteger(roundingInstallmentToNearestInteger)
                .monthlyRepaymentFrequencyDay(monthlyRepaymentFrequencyDay)
                .serviceChargeRatePrecision(serviceChargeRatePrecision)
                .principalAmountPrecision(principalAmountPrecision)
                .installmentAmountPrecision(installmentAmountPrecision)
                .cutOffDate(cutOffDate)
                .noOfPastInstallments(noOfPastInstallments)
                .build();
    }
}
