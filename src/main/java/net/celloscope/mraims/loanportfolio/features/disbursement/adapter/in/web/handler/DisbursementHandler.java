package net.celloscope.mraims.loanportfolio.features.disbursement.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.enums.RepaymentScheduleEnum;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.disbursement.application.port.in.DisbursementUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class DisbursementHandler {

    private final DisbursementUseCase disbursementUseCase;

    public Mono<ServerResponse> disburseLoan(ServerRequest serverRequest) {
        String loanAccountId = serverRequest.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse("");
        LocalDate disbursementDate = LocalDate.parse(serverRequest.queryParam(QueryParams.DISBURSEMENT_DATE.getValue()).orElse(LocalDate.now().toString()), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        String loginId = serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");
        String officeId = serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("");
        String serviceChargeCalculationMethod = serverRequest.queryParam(QueryParams.SERVICE_CHARGE_CALCULATION_METHOD.getValue()).orElse(RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE.getValue());

        return disbursementUseCase.disburseLoan(loanAccountId, disbursementDate, loginId, officeId, serviceChargeCalculationMethod)
                .flatMap(repaymentScheduleResponseDTOS ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(repaymentScheduleResponseDTOS))
                .onErrorMap(this::buildResponseStatusException);
    }


    public Mono<ServerResponse> disburseLoanMigration(ServerRequest serverRequest) {
        String loanAccountId = serverRequest.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse("");
        LocalDate disbursementDate = LocalDate.parse(serverRequest.queryParam(QueryParams.DISBURSEMENT_DATE.getValue()).orElse(LocalDate.now().toString()), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        String loginId = serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");
        String officeId = serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("");
        String serviceChargeCalculationMethod = serverRequest.queryParam(QueryParams.SERVICE_CHARGE_CALCULATION_METHOD.getValue()).orElse(RepaymentScheduleEnum.SERVICE_CHARGE_CALCULATION_METHOD_DECLINING_BALANCE.getValue());
        LocalDate cutOffDate = LocalDate.parse(serverRequest.queryParam(QueryParams.CUT_OFF_DATE.getValue()).orElse(LocalDate.now().toString()), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        Integer noOfPastInstallments = Integer.parseInt(serverRequest.queryParam(QueryParams.NO_OF_PAST_INSTALLMENTS.getValue()).orElse("0"));
        BigDecimal installmentAmount = new BigDecimal(serverRequest.queryParam(QueryParams.INSTALLMENT_AMOUNT.getValue()).orElse("0"));
        BigDecimal disbursedLoanAmount = new BigDecimal(serverRequest.queryParam(QueryParams.DISBURSED_LOAN_AMOUNT.getValue()).orElse("0"));


        return disbursementUseCase.disburseLoanMigration(loanAccountId, disbursementDate, loginId, officeId, serviceChargeCalculationMethod, cutOffDate, noOfPastInstallments, installmentAmount, disbursedLoanAmount, false, 12)
                .flatMap(repaymentScheduleResponseDTOS ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(repaymentScheduleResponseDTOS))
                .onErrorMap(this::buildResponseStatusException);
    }

    public ResponseStatusException buildResponseStatusException(Throwable throwable) {
        if (throwable instanceof ExceptionHandlerUtil) {
            return new ResponseStatusException(((ExceptionHandlerUtil) throwable).getCode(), throwable.getMessage());
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, throwable.getMessage());
    }
}
