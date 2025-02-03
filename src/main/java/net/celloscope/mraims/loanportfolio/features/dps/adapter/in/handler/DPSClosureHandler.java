package net.celloscope.mraims.loanportfolio.features.dps.adapter.in.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.DPSClosureUseCase;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto.DPSAuthorizeCommand;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto.DPSClosureCommand;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto.DPSGridViewCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

@Component
@Slf4j
public class DPSClosureHandler {
    private final DPSClosureUseCase dpsClosureUseCase;

    public DPSClosureHandler(DPSClosureUseCase dpsClosureUseCase) {
        this.dpsClosureUseCase = dpsClosureUseCase;
    }


    public Mono<ServerResponse> getDPSGridViewByOffice(ServerRequest serverRequest) {
        return validateRequestForDPSGridView(serverRequest)
                .flatMap(serverRequest1 -> dpsClosureUseCase.getDPSGridViewByOffice(buildGridViewCommand(serverRequest1)))
                .flatMap(dpsGridViewDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsGridViewDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getDPSAuthorizationGridViewByOffice(ServerRequest serverRequest) {
        return validateRequestForDPSGridView(serverRequest)
                .flatMap(serverRequest1 -> dpsClosureUseCase.getDPSClosureGridViewByOffice(buildGridViewCommand(serverRequest1)))
                .flatMap(dpsGridViewDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsGridViewDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private Mono<ServerRequest> validateRequestForDPSGridView(ServerRequest serverRequest){
        return Mono.just(serverRequest)
                .filter(request -> !HelperUtil.checkIfNullOrEmpty(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "OfficeId is Required!")));
    }

    private DPSGridViewCommand buildGridViewCommand(ServerRequest serverRequest) {
        String officeId = serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("");
        String mfiId = serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse("");
        String loginId = serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");
        String searchText = serverRequest.queryParam(QueryParams.SEARCH_TEXT.getValue()).orElse("").replace("%20", "").trim();
        Integer limit = Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10"));
        Integer offset = Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0"));

        return DPSGridViewCommand
                .builder()
                .officeId(officeId)
                .mfiId(mfiId)
                .loginId(loginId)
                .searchText(searchText)
                .limit(limit)
                .offset(offset)
                .build();
    }

    public Mono<ServerResponse> getDPSDetailViewBySavingsAccountId(ServerRequest serverRequest) {
        return validateRequestForDPSDetailView(serverRequest)
                .flatMap(serverRequest1 -> dpsClosureUseCase
                        .getDPSDetailViewBySavingsAccountId(serverRequest1.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("")))
                .flatMap(dpsDetailViewDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsDetailViewDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getDPSClosureDetailViewBySavingsAccountId(ServerRequest serverRequest) {
        return validateRequestForDPSDetailView(serverRequest)
                .flatMap(serverRequest1 -> dpsClosureUseCase
                        .getDPSClosureDetailViewBySavingsAccountId(serverRequest1.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("")))
                .flatMap(dpsClosureDetailViewResponse -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsClosureDetailViewResponse))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    private Mono<ServerRequest> validateRequestForDPSDetailView(ServerRequest serverRequest){
        return Mono.just(serverRequest)
                .filter(request -> !HelperUtil.checkIfNullOrEmpty(request.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SavingsAccountId is Required!")));
    }


    public Mono<ServerResponse> closeDPSAccount(ServerRequest serverRequest) {
        return validateRequestForDPSClosure(serverRequest)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    log.info("command : {}", command);
                    return command;
                })
                .flatMap(dpsClosureUseCase::closeDPSAccount)
                .flatMap(dpsClosureDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsClosureDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> getDPSAccountClosingInfo(ServerRequest serverRequest) {
        return buildDPSClosingInfoCommand(serverRequest)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    log.info("command : {}", command);
                    return command;
                })
                .flatMap(dpsClosureUseCase::getDPSClosingInfoBySavingsAccountId)
                .flatMap(dpsClosureDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsClosureDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    Mono<DPSClosureCommand> buildDPSClosingInfoCommand(ServerRequest serverRequest) {
        DPSClosureCommand dpsClosureCommand = DPSClosureCommand
                .builder()
                .savingsAccountId(serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse(""))
                .closingDate(LocalDate.parse(serverRequest.queryParam(QueryParams.CLOSING_DATE.getValue()).orElse(""), DateTimeFormatter.ofPattern(net.celloscope.mraims.loanportfolio.core.util.Constants.DATE_FORMAT_yyyy_MM_dd)))
                .effectiveInterestRate(BigDecimal.valueOf(Double.parseDouble(serverRequest.queryParam(QueryParams.EFFECTIVE_INTEREST_RATE.getValue()).orElse(""))))
                .build();
        return Mono.just(dpsClosureCommand);
    }


    private Mono<DPSClosureCommand> validateRequestForDPSClosure(ServerRequest serverRequest){
        return serverRequest.bodyToMono(DPSClosureCommand.class)
                .flatMap(command -> {
                    if (command.getSavingsAccountId() == null)
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SavingsAccountId cannot be empty!"));
                    else if (command.getClosingDate() == null)
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Closing Date cannot be empty!"));
                    else if (!command.getPaymentMode().equalsIgnoreCase(Constants.PAYMENT_MODE_CASH.getValue()) && command.getReferenceAccountId() == null)
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Reference Account Id Required!"));
                    else
                        return Mono.just(command);
                });
    }


    public Mono<ServerResponse> authorizeDPSAccountClosure(ServerRequest serverRequest) {
        return validateRequestForDPSClosureAuthorization(serverRequest)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    command.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    log.info("command : {}", command);
                    return command;
                })
                .flatMap(dpsClosureUseCase::authorizeDPSClosure)
                .flatMap(dpsClosureDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsClosureDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> rejectDPSAccountClosure(ServerRequest serverRequest) {
        return validateRequestForDPSClosureAuthorization(serverRequest)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    command.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    log.info("command : {}", command);
                    return command;
                })
                .flatMap(dpsClosureUseCase::rejectDPSClosure)
                .flatMap(dpsClosureDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsClosureDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    private Mono<DPSAuthorizeCommand> validateRequestForDPSClosureAuthorization(ServerRequest serverRequest){
        return serverRequest.bodyToMono(DPSAuthorizeCommand.class)
                .flatMap(command -> command.getSavingsAccountId() == null
                        ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SavingsAccountId cannot be empty!"))
                        : Mono.just(command));
    }
}
