package net.celloscope.mraims.loanportfolio.features.savingsclosure.adapter.in.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in.SavingsClosureUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsclosure.application.port.in.dto.SavingsClosureCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
public class SavingsClosureHandler {

    private final SavingsClosureUseCase savingsClosureUseCase;

    public SavingsClosureHandler(SavingsClosureUseCase savingsClosureUseCase) {
        this.savingsClosureUseCase = savingsClosureUseCase;
    }

    public Mono<ServerResponse> closeSavingsAccount(ServerRequest serverRequest) {
        return validateRequestForSavingsEnCashment(serverRequest)
                .map(savingsClosureCommand -> {
                    savingsClosureCommand.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty!")));
                    savingsClosureCommand.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office ID cannot be empty!")));
                    return savingsClosureCommand;
                })
                .flatMap(savingsClosureUseCase::closeSavingsAccount)
                .flatMap(savingsClosureDto -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savingsClosureDto))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private Mono<SavingsClosureCommand> validateRequestForSavingsEnCashment(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SavingsClosureCommand.class)
                .flatMap(command -> {
                    if (command.getSavingsAccountId() == null)
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "SavingsAccountId cannot be empty!"));
                    else if (command.getClosingDate() == null)
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Closing Date cannot be empty!"));
                    else
                        return Mono.just(command);
                });
    }

    public Mono<ServerResponse> authorizeSavingsAccountClosure(ServerRequest serverRequest) {
        return buildSavingsClosureCommand(serverRequest)
                .flatMap(savingsClosureUseCase::authorizeSavingsClosure)
                .flatMap(savingsClosureDto -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savingsClosureDto))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> rejectSavingsAccountClosure(ServerRequest serverRequest) {
        return buildSavingsClosureCommand(serverRequest)
                .flatMap(savingsClosureUseCase::rejectSavingsClosure)
                .flatMap(savingsClosureDto -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(savingsClosureDto))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private Mono<SavingsClosureCommand> buildSavingsClosureCommand(ServerRequest serverRequest) {
        return validateRequestForSavingsClosureAuthorization(serverRequest)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty!")));
                    command.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElseThrow(() -> new ServerWebInputException("MFI ID cannot be empty!")));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office ID cannot be empty!")));
                    log.info("Savings Closure Command : {}", command);
                    return command;
                });
    }

    private Mono<SavingsClosureCommand> validateRequestForSavingsClosureAuthorization(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SavingsClosureCommand.class)
                .flatMap(command -> command.getSavingsAccountId() == null
                        ? Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Savings Account Id cannot be empty!"))
                        : Mono.just(command));
    }
}
