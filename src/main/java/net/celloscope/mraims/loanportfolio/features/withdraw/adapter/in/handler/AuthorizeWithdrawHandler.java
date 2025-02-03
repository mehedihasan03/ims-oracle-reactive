package net.celloscope.mraims.loanportfolio.features.withdraw.adapter.in.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.AuthorizeCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.AuthorizeWithdrawUseCase;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands.AuthorizeWithdrawCommand;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
public class AuthorizeWithdrawHandler {

    private final AuthorizeWithdrawUseCase authorizeWithdrawUseCase;

    public AuthorizeWithdrawHandler(AuthorizeWithdrawUseCase authorizeWithdrawUseCase) {
        this.authorizeWithdrawUseCase = authorizeWithdrawUseCase;
    }

    public Mono<ServerResponse> authorizeWithdraw(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(AuthorizeWithdrawCommand.class)
                .flatMap(authorizeWithdrawUseCase::authorizeWithdraw)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received for authorize withdraw: {}", r))
                .doOnSuccess(res -> log.info("Response for authorize withdraw: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing authorize withdraw request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));

    }
}
