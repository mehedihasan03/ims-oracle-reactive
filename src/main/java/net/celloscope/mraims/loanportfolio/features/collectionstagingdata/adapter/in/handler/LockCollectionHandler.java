package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.LockCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.LockCollectionCommand;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class LockCollectionHandler {

    private final LockCollectionUseCase useCase;

    public Mono<ServerResponse> lockCollection(ServerRequest serverRequest) {
        String lockedBy = serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse("");
        return serverRequest.bodyToMono(LockCollectionCommand.class)
                .mapNotNull(command -> {
                    command.setLoginId(lockedBy);
                    return command;
                })
                .flatMap(useCase::lockCollectionBySamity)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received to lock collection: {}", r))
                .doOnSuccess(res -> log.info("Response for locking collection: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing lock collection request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> unlockCollection(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LockCollectionCommand.class)
                .flatMap(useCase::unlockCollectionBySamity)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received to unlock collection: {}", r))
                .doOnSuccess(res -> log.info("Response for unlocking collection: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing unlock collection request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }
}
