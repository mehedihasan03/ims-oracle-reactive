package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.ResetCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.ResetCollectionCommand;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResetCollectionHandler {

    private final ResetCollectionUseCase useCase;

    public Mono<ServerResponse> resetCollection(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(ResetCollectionCommand.class)
                .mapNotNull(command -> {
                    command.setInstituteOid(serverRequest.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElse(""));
                    command.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return command;
                })
                .flatMap(useCase::resetCollection)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received to reset collection: {}", r))
                .doOnSuccess(res -> log.info("Response for resetting collection: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing reset collection request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> resetSpecialCollection(ServerRequest serverRequest) {
        String oid = serverRequest.queryParam("oid").orElseThrow(() -> new ServerWebInputException("oid is required"));
        return useCase.resetSpecialCollection(oid)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received to delete collection staging data: {}", r))
                .doOnSuccess(res -> log.info("Response for deleting collection staging data: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing delete collection staging data request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }
}
