package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.AuthorizeCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.AuthorizeCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.RejectionCollectionCommand;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.UnauthorizeCollectionCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthorizeCollectionHandler {

    private final AuthorizeCollectionUseCase authorizeCollectionUseCase;

    public Mono<ServerResponse> authorizeCollection(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(AuthorizeCollectionCommand.class)
                .map(authorizeCollectionCommand -> {
                    if(HelperUtil.checkIfNullOrEmpty(authorizeCollectionCommand.getOfficeId())){
                        authorizeCollectionCommand.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    }
                    if(HelperUtil.checkIfNullOrEmpty(authorizeCollectionCommand.getLoginId())){
                        authorizeCollectionCommand.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    }
                    return authorizeCollectionCommand;
                })
                .flatMap(authorizeCollectionUseCase::authorize)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received for authorize collection: {}", r))
                .doOnSuccess(res -> log.info("Response for authorize collection: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing authorize collection request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));

    }

    public Mono<ServerResponse> rejectCollection(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(RejectionCollectionCommand.class)
                .map(rejectionCollectionCommand -> {
                    rejectionCollectionCommand.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return rejectionCollectionCommand;
                })
                .flatMap(authorizeCollectionUseCase::reject)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received for reject collection: {}", r))
                .doOnSuccess(res -> log.info("Response for reject collection: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing rejection collection request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> unauthorizeCollection(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UnauthorizeCollectionCommand.class)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    command.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return command;
                })
                .filter(command -> !HelperUtil.checkIfNullOrEmpty(command.getMfiId()) && !HelperUtil.checkIfNullOrEmpty(command.getLoginId()) && !HelperUtil.checkIfNullOrEmpty(command.getOfficeId()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "mfiId, loginId and officeId are required in queryParam")))
                .flatMap(authorizeCollectionUseCase::unauthorize)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received for unauthorize collection: {}", r))
                .doOnSuccess(res -> log.info("Response for unauthorize collection: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing unauthorize collection request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));

    }
}
