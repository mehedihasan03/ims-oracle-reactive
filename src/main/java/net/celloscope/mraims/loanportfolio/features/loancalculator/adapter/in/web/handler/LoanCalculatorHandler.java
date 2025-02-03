package net.celloscope.mraims.loanportfolio.features.loancalculator.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.LoanCalculatorUseCase;
import net.celloscope.mraims.loanportfolio.features.loancalculator.application.port.in.dto.request.LoanCalculatorRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Predicate;


@Component
@Slf4j
@RequiredArgsConstructor
public class LoanCalculatorHandler {
    private final LoanCalculatorUseCase loanCalculatorUseCase;

    public Mono<ServerResponse> getLoanProductsByMfi(ServerRequest serverRequest) {
        String instituteOid = serverRequest.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElse("");
        log.info("instituteOid: {}", instituteOid);

        return loanCalculatorUseCase
                .getActiveLoanProductsByMfi(instituteOid)
                .flatMap(loanProductListResponseDTO ->
                        ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loanProductListResponseDTO))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getLoanProductInfoByLoanProductId(ServerRequest serverRequest) {
        String loanProductId = serverRequest.queryParam(QueryParams.LOAN_PRODUCT_ID.getValue()).orElse("");
        log.info("loanProductId: {}", loanProductId);
        return loanCalculatorUseCase
                .getLoanProductInfo(loanProductId)
                .flatMap(loanProductInfoResponseDTO ->
                        ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(loanProductInfoResponseDTO))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> getLoanRepaySchedule(ServerRequest serverRequest) {
        return loanCalculatorUseCase
                .generateRepaymentScheduleForLoan(buildLoanCalculatorRequestDTO(serverRequest))
                .flatMap(loanProductInfoResponseDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loanProductInfoResponseDTO))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private LoanCalculatorRequestDTO buildLoanCalculatorRequestDTO(ServerRequest serverRequest) {
        LocalDate disbursementDate;
        try {
            String disbursementDateString = serverRequest.queryParam(QueryParams.DISBURSEMENT_DATE.getValue()).orElseThrow(() -> new IllegalArgumentException("Disbursement date is required"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd);
            disbursementDate = LocalDate.parse(disbursementDateString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Disbursement date must be in the format YYYY-MM-dd", e);
        }

        String installmentAmount = serverRequest.queryParam(QueryParams.INSTALLMENT_AMOUNT.getValue()).orElse("");
        log.info("installmentAmount String: {}", installmentAmount);
        BigDecimal installmentAmountValue;
        if (installmentAmount.isBlank() || installmentAmount.isEmpty()) {
            installmentAmountValue = BigDecimal.ZERO;
        } else {
            installmentAmountValue = new BigDecimal(installmentAmount);
        }

        log.info("installmentAmountValue: {}", installmentAmountValue);

        int roundingToNearestInteger;
        try {
            roundingToNearestInteger = Integer.parseInt(serverRequest.queryParam(QueryParams.ROUNDING_TO_NEAREST_INTEGER.getValue()).orElse("0"));
        } catch (NumberFormatException e) {
            roundingToNearestInteger = 0;
        }

        return LoanCalculatorRequestDTO.builder()
                .loanProductId(serverRequest.queryParam(QueryParams.LOAN_PRODUCT_ID.getValue()).orElse(""))
                .loanAmount(new BigDecimal(serverRequest.queryParam(QueryParams.LOAN_AMOUNT.getValue()).orElse("0")))
                .noOfInstallments(Integer.parseInt(serverRequest.queryParam(QueryParams.NO_OF_INSTALLMENTS.getValue()).orElse("0")))
                .graceDays(Integer.parseInt(serverRequest.queryParam(QueryParams.GRACE_DAYS.getValue()).orElse("0")))
                .disbursementDate(disbursementDate)
                .samityDay(serverRequest.queryParam(QueryParams.SAMITY_DAY.getValue()).orElse(""))
                .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                .roundingToNearestInteger(roundingToNearestInteger)
//                .installmentAmount(new BigDecimal((serverRequest.queryParam(QueryParams.INSTALLMENT_AMOUNT.getValue())).orElse("0")))
                .installmentAmount(installmentAmountValue)
                .loanTermInMonths(Integer.parseInt(serverRequest.queryParam(QueryParams.LOAN_TERM_IN_MONTHS.getValue()).orElse("12")))
                .build();
    }

}
