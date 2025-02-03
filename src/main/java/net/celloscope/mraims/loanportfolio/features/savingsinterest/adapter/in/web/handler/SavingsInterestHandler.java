package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.in.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.common.queries.repository.CommonRepository;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.AccruedInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.SavingsInterestUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.AccruedInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.CalculateInterestCommand;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.request.PostSavingsInterestCommand;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Predicate;

@Component
@Slf4j
public class SavingsInterestHandler {

    private final SavingsInterestUseCase savingsInterestUseCase;
    private final AccruedInterestUseCase accruedInterestUseCase;
    private final CommonRepository commonRepository;

    public SavingsInterestHandler(SavingsInterestUseCase savingsInterestUseCase, AccruedInterestUseCase accruedInterestUseCase, CommonRepository commonRepository) {
        this.savingsInterestUseCase = savingsInterestUseCase;
        this.accruedInterestUseCase = accruedInterestUseCase;
        this.commonRepository = commonRepository;
    }

    public Mono<ServerResponse> calculateDailyAccruedInterest(ServerRequest serverRequest) {
        return savingsInterestUseCase
                .calculateDailyAccruedInterest(buildDailyInterestCommand(serverRequest))
                .flatMap(savingsInterestResponseDTO ->
                        ServerResponse
                        .ok()
                        .bodyValue(savingsInterestResponseDTO))
                .doOnRequest(r -> log.info("Request Received for calculateSavingsInterest: {}", r))
                .doOnSuccess(res -> log.info("Response for calculateSavingsInterest: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing calculateSavingsInterest request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> calculateMonthlySavingsAccruedInterest(ServerRequest serverRequest) {

        return savingsInterestUseCase
                .calculateMonthlyAccruedInterest(buildMonthlyAccruedInterestCommand(serverRequest))
                .flatMap(savingsAccruedInterestResponseDTO -> ServerResponse
                        .ok()
                        .bodyValue(savingsAccruedInterestResponseDTO))
                .doOnRequest(r -> log.info("Request Received for calculateSavingsInterest: {}", r))
                .doOnSuccess(res -> log.info("Response for calculateSavingsInterest: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing calculateSavingsInterest request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> calculateDPSMaturityAmount(ServerRequest serverRequest) {

        return savingsInterestUseCase
                .calculateDPSMaturityAmountWithoutCompounding(serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse(""))
                .flatMap(savingsAccruedInterestResponseDTO -> ServerResponse
                        .ok()
                        .bodyValue(savingsAccruedInterestResponseDTO))
                .doOnRequest(r -> log.info("Request Received for calculating dps maturity amount: {}", r))
                .doOnSuccess(res -> log.info("Response for calculating dps maturity amount: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while calculating dps maturity amount: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> postSavingsInterest(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(PostSavingsInterestCommand.class)
                .flatMap(savingsInterestUseCase::postSavingsInterest)
                .flatMap(string -> ServerResponse
                        .ok()
                        .bodyValue(string))
                .doOnRequest(r -> log.info("Request Received for postSavingsInterest: {}", r))
                .doOnSuccess(res -> log.info("Response for postSavingsInterest: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing postSavingsInterest request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> calculateAndSaveMonthlyAccruedInterest(ServerRequest serverRequest) {

        return accruedInterestUseCase
                .saveAccruedInterest(buildSaveMonthlySavingsAccruedInterestCommand(serverRequest))
                .flatMap(savingsAccruedInterestResponseDTO -> ServerResponse
                        .ok()
                        .bodyValue(savingsAccruedInterestResponseDTO))
                .doOnRequest(r -> log.info("Request Received for calculateSavingsInterest: {}", r))
                .doOnSuccess(res -> log.info("Response for calculateSavingsInterest: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing calculateSavingsInterest request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> calculateMonthlyAccruedInterest(ServerRequest serverRequest) {
        return accruedInterestUseCase
                .calculateMonthlyAccruedInterest(serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse(""),
                        Integer.valueOf(serverRequest.queryParam(QueryParams.INTEREST_CALCULATION_MONTH.getValue()).orElse("")),
                        Integer.valueOf(serverRequest.queryParam(QueryParams.INTEREST_CALCULATION_YEAR.getValue()).orElse("")),
                        LocalDate.parse(serverRequest.queryParam("businessDate").orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd))
                )
                .flatMap(savingsAccruedInterestResponseDTO -> ServerResponse
                        .ok()
                        .bodyValue(savingsAccruedInterestResponseDTO))
                .doOnRequest(r -> log.info("Request Received for calculateSavingsInterest: {}", r))
                .doOnSuccess(res -> log.info("Response for calculateSavingsInterest: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing calculateSavingsInterest request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getAccruedInterestEntities(ServerRequest serverRequest) {
        String savingsAccountId = serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");
        Integer year = Integer.valueOf(serverRequest.queryParam(QueryParams.INTEREST_CALCULATION_YEAR.getValue()).orElse(""));
        String closingType = serverRequest.queryParam(QueryParams.CLOSING_TYPE.getValue()).orElse("");

        return accruedInterestUseCase
                .getAccruedInterestEntriesBySavingsAccountIdYearAndClosingType(savingsAccountId, year, closingType)
                .collectList()
                .flatMap(savingsAccruedInterestResponseDTOList -> ServerResponse
                        .ok()
                        .bodyValue(savingsAccruedInterestResponseDTOList))
                .doOnError(err -> log.info("Error occurred while processing calculateSavingsInterest request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private CalculateInterestCommand buildMonthlyAccruedInterestCommand(ServerRequest serverRequest) {
        String savingsAccountId = serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");
        Integer interestCalculationMonth = Integer.valueOf(serverRequest.queryParam(QueryParams.INTEREST_CALCULATION_MONTH.getValue()).orElse(""));
        Integer interestCalculationYear = Integer.valueOf(serverRequest.queryParam(QueryParams.INTEREST_CALCULATION_YEAR.getValue()).orElse(""));

        return CalculateInterestCommand
                .builder()
                .savingsAccountId(savingsAccountId)
                .interestCalculationMonth(interestCalculationMonth)
                .interestCalculationYear(interestCalculationYear)
                .build();
    }

    private AccruedInterestCommand buildSaveMonthlySavingsAccruedInterestCommand(ServerRequest serverRequest) {
        String savingsAccountId = serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");
        Integer interestCalculationMonth = Integer.valueOf(serverRequest.queryParam(QueryParams.INTEREST_CALCULATION_MONTH.getValue()).orElse(""));
        Integer interestCalculationYear = Integer.valueOf(serverRequest.queryParam(QueryParams.INTEREST_CALCULATION_YEAR.getValue()).orElse(""));
        String loginId = serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");
        /*String managementProcessId = serverRequest.queryParam(QueryParams.MANAGEMENT_PROCESS_ID.getValue()).orElse("");
        String mfiId = serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse("");*/

        return AccruedInterestCommand
                .builder()
                .savingsAccountId(savingsAccountId)
                .interestCalculationMonth(interestCalculationMonth)
                .interestCalculationYear(interestCalculationYear)
                .loginId(loginId)
               /* .managementProcessId(managementProcessId)
                .mfiId(mfiId)*/
                .build();
    }

    private CalculateInterestCommand buildDailyInterestCommand(ServerRequest serverRequest) {
        String savingsAccountId = serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");
        LocalDate interestCalculationDate = LocalDate.parse(serverRequest.queryParam(QueryParams.INTEREST_CALCULATION_DATE.getValue()).orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));


        return CalculateInterestCommand
                .builder()
                .savingsAccountId(savingsAccountId)
                .interestCalculationDate(interestCalculationDate)
                .build();
    }


}
