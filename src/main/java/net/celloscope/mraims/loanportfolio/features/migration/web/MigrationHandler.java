package net.celloscope.mraims.loanportfolio.features.migration.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.CancelledSamityUseCase;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.request.CancelSamityRequestDTO;
import net.celloscope.mraims.loanportfolio.features.migration.MigrationService;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigratedCollectionResponseDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationCollectionRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationRequestDto;
import net.celloscope.mraims.loanportfolio.features.migration.dtos.MigrationResponseDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationHandler {

    private final MigrationService migrationService;

    public Mono<ServerResponse> migrate(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MigrationRequestDto.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .doOnNext(requestDTO -> log.info("Migration Request Dto : {}", requestDTO))
                .flatMap(requestDTO -> Mono.fromCallable(() -> migrationService.migrate(requestDTO)))
                .doOnRequest(request -> log.debug("Request received to migrate : {}", request))
                .doOnSuccess(serverResponse -> log.info("Response for migration : {}", serverResponse))
                .doOnError(err -> log.error("Error occurred while migration : {}", err.getMessage()))
                .flatMap(responseDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(responseDTO, MigrationResponseDto.class)
                )
                .doOnSuccess(response -> log.info("Response for migration : {}", response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest));
    }

    public Mono<ServerResponse> cutOffDateCollection(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MigrationCollectionRequestDto.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .doOnNext(requestDTO -> log.info("Migration Request Dto : {}", requestDTO))
                .flatMap(migrationService::migrateCutOffDateCollection)
                .doOnRequest(request -> log.debug("Request received to migrate : {}", request))
                .doOnSuccess(serverResponse -> log.info("Response for migration : {}", serverResponse))
                .doOnError(err -> log.error("Error occurred while migration : {}", err.getMessage()))
                .flatMap(responseDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(responseDTO))
                )
                .doOnSuccess(response -> log.info("Response for migration collection : {}", response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest));
    }
}
