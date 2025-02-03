package net.celloscope.mraims.loanportfolio.features.fdr.adapter.in.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.dps.application.port.in.dto.DPSClosureCommand;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.FDRUseCase;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.FDRAuthorizeCommand;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.FDRClosureCommand;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.FDRGridViewCommand;
import net.celloscope.mraims.loanportfolio.features.fdr.application.port.in.dto.FDRRequestDTO;
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
public class FDRHandler {
    private final FDRUseCase fdrUseCase;

    public FDRHandler(FDRUseCase fdrUseCase) {
        this.fdrUseCase = fdrUseCase;
    }

    public Mono<ServerResponse> activateFDRAccount(ServerRequest serverRequest) {
        /*String savingsAccountId = serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");
        BigDecimal fdrAmount = BigDecimal.valueOf(Long.parseLong(serverRequest.queryParam(QueryParams.AMOUNT.getValue()).orElse("")));
        LocalDate activationDate = LocalDate.parse(serverRequest.queryParam(QueryParams.FDR_ACTIVATION_DATE.getValue()).orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        String loginId = serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");*/

        return serverRequest
                .bodyToMono(FDRRequestDTO.class)
                .flatMap(fdrUseCase::activateFDRAccount2)
                .flatMap(fdrSchedule -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrSchedule))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> postFDRInterest(ServerRequest serverRequest) {
        LocalDate interestPostingDate = LocalDate.parse(serverRequest.queryParam(QueryParams.INTEREST_POSTING_DATE.getValue()).orElse(""), DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_yyyy_MM_dd));
        String loginId = serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");
        String officeId = serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("");

        return fdrUseCase
                .postFDRInterestToAccount(interestPostingDate, loginId, officeId)
                .collectList()
                .flatMap(fdrSchedule -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrSchedule))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> getFDRInterestPostingSchedule(ServerRequest serverRequest) {
        String savingsAccountId = serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");

        return fdrUseCase
                .getFDRSchedule(savingsAccountId)
                .flatMap(fdrSchedule -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrSchedule))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getFDRGridViewByOffice(ServerRequest serverRequest) {
        return validateRequestForFDRGridView(serverRequest)
                .flatMap(serverRequest1 -> fdrUseCase.getFDRGridViewByOffice(buildGridViewCommand(serverRequest1)))
                .flatMap(fdrGridViewDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrGridViewDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private Mono<ServerRequest> validateRequestForFDRGridView(ServerRequest serverRequest){
        return Mono.just(serverRequest)
                .filter(request -> !HelperUtil.checkIfNullOrEmpty(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "OfficeId is Required!")));
    }

    private FDRGridViewCommand buildGridViewCommand(ServerRequest serverRequest) {
        String officeId = serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("");
        Integer limit = Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10"));
        Integer offset = Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0"));
        String searchText = serverRequest.queryParam(QueryParams.SEARCH_TEXT.getValue()).orElse("").replace("%20", "").trim();

        return FDRGridViewCommand
                .builder()
                .officeId(officeId)
                .limit(limit)
                .offset(offset)
                .searchText(searchText)
                .build();
    }


    public Mono<ServerResponse> getFDRDetailViewBySavingsAccountId(ServerRequest serverRequest) {
        return validateRequestForFDRDetailView(serverRequest)
                .flatMap(serverRequest1 -> fdrUseCase.getFDRDetailViewByAccountId(serverRequest1.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("")))
                .flatMap(fdrDetailViewDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrDetailViewDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getFDRAuthorizationGridViewByOffice(ServerRequest serverRequest) {
        return validateRequestForFDRGridView(serverRequest)
                .flatMap(serverRequest1 -> fdrUseCase.getFDRClosureGridViewByOffice(buildGridViewCommand(serverRequest1)))
                .flatMap(fdrClosureGridViewResponse -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrClosureGridViewResponse))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getFDRClosureDetailViewBySavingsAccountId(ServerRequest serverRequest) {
        return validateRequestForFDRDetailView(serverRequest)
                .flatMap(serverRequest1 -> fdrUseCase
                        .getFDRClosureDetailViewBySavingsAccountId(serverRequest1.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("")))
                .flatMap(fdrClosureDetailViewResponse -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrClosureDetailViewResponse))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getFDRAccountClosingInfo(ServerRequest serverRequest) {
        return this.buildFDRClosingInfoCommand(serverRequest)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    log.info("command : {}", command);
                    return command;
                })
                .flatMap(fdrUseCase::getFDRClosingInfoBySavingsAccountId)
                .flatMap(dpsClosureDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(dpsClosureDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    private Mono<ServerRequest> validateRequestForFDRDetailView(ServerRequest serverRequest){
        return Mono.just(serverRequest)
                .filter(request -> !HelperUtil.checkIfNullOrEmpty(request.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("")))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SavingsAccountId is Required!")));
    }


    public Mono<ServerResponse> closeFDRAccount(ServerRequest serverRequest) {
        return validateRequestForFDREncashment(serverRequest)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    log.info("command : {}", command);
                    return command;
                    })
                .flatMap(fdrUseCase::closeFDRAccount)
                .flatMap(fdrEncashmentDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrEncashmentDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private Mono<FDRClosureCommand> validateRequestForFDREncashment(ServerRequest serverRequest){
        return serverRequest.bodyToMono(FDRClosureCommand.class)
                .flatMap(command -> {
                    if (command.getSavingsAccountId() == null)
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SavingsAccountId cannot be empty!"));
                    else if (command.getEncashmentDate() == null)
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Encashment Date cannot be empty!"));
                    else if (!command.getPaymentMode().equalsIgnoreCase("CASH") && command.getEffectiveInterestRate() == null)
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Effective Interest Rate cannot be empty!"));
                    else
                        return Mono.just(command);
                });
    }


    public Mono<ServerResponse> authorizeFDRAccountClosure(ServerRequest serverRequest) {
        return validateRequestForFDRClosureAuthorization(serverRequest)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    command.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    log.info("command : {}", command);
                    return command;
                })
                .flatMap(fdrUseCase::authorizeFDRClosure)
                .flatMap(fdrEncashmentDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrEncashmentDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> rejectFDRAccountClosure(ServerRequest serverRequest) {
        return validateRequestForFDRClosureAuthorization(serverRequest)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    command.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    log.info("command : {}", command);
                    return command;
                })
                .flatMap(fdrUseCase::rejectFDRClosure)
                .flatMap(fdrEncashmentDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(fdrEncashmentDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    private Mono<FDRAuthorizeCommand> validateRequestForFDRClosureAuthorization(ServerRequest serverRequest){
        return serverRequest
                .bodyToMono(FDRAuthorizeCommand.class)
                .flatMap(command -> command.getSavingsAccountId() == null
                        ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SavingsAccountId cannot be empty!"))
                        : Mono.just(command));
    }

    private FDRClosureCommand buildFDREncashmentCommand(ServerRequest serverRequest) {
        String savingsAccountId = serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse("");
        LocalDate encashmentDate = LocalDate.parse(serverRequest.queryParam(QueryParams.ENCASHMENT_DATE.getValue()).orElse(""), DateTimeFormatter.ofPattern(net.celloscope.mraims.loanportfolio.core.util.Constants.DATE_FORMAT_yyyy_MM_dd));

        BigDecimal effectiveInterestRate = BigDecimal.valueOf(Long.parseLong(serverRequest.queryParam(QueryParams.EFFECTIVE_INTEREST_RATE.getValue()).orElse("")));
        String paymentMode = serverRequest.queryParam(QueryParams.PAYMENT_MODE.getValue()).orElse("");
        String referenceAccountId= serverRequest.queryParam(QueryParams.REFERENCE_ACCOUNT_ID.getValue()).orElse("");
        String loginId= serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");

        return FDRClosureCommand
                .builder()
                .savingsAccountId(savingsAccountId)
                .encashmentDate(encashmentDate)
                .effectiveInterestRate(effectiveInterestRate)
                .paymentMode(paymentMode)
                .referenceAccountId(referenceAccountId)
                .loginId(loginId)
                .build();

    }

    Mono<FDRClosureCommand> buildFDRClosingInfoCommand(ServerRequest serverRequest) {
        FDRClosureCommand fdrClosureCommand = FDRClosureCommand
                .builder()
                .savingsAccountId(serverRequest.queryParam(QueryParams.SAVINGS_ACCOUNT_ID.getValue()).orElse(""))
                .encashmentDate(LocalDate.parse(serverRequest.queryParam(QueryParams.CLOSING_DATE.getValue()).orElse(""), DateTimeFormatter.ofPattern(net.celloscope.mraims.loanportfolio.core.util.Constants.DATE_FORMAT_yyyy_MM_dd)))
                .effectiveInterestRate(BigDecimal.valueOf(Double.parseDouble(serverRequest.queryParam(QueryParams.EFFECTIVE_INTEREST_RATE.getValue()).orElse(""))))
                .build();
        return Mono.just(fdrClosureCommand);
    }


}
