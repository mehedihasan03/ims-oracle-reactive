package net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.DataArchiveUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdataarchive.application.port.in.helpers.dto.commands.DataArchiveCommandDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataArchiveHandler {

    private final DataArchiveUseCase useCase;

    public Mono<ServerResponse> archiveData(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(DataArchiveCommandDto.class)
                .flatMap(useCase::archive)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received for collection staging data archive: {}", r))
                .doOnSuccess(res -> log.info("Response for collection staging data archive: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing collection staging data archive request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));

    }
}
