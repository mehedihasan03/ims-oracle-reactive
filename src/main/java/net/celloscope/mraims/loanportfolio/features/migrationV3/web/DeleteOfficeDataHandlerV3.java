package net.celloscope.mraims.loanportfolio.features.migrationV3.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.migrationV3.deleteofficedata.DeleteOfficeDataServiceV3;
import net.celloscope.mraims.loanportfolio.features.migrationV3.dtos.OfficeDataDeleteRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeleteOfficeDataHandlerV3 {
    private final DeleteOfficeDataServiceV3 service;

    public Mono<ServerResponse> deleteOfficeData(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(OfficeDataDeleteRequestDTO.class)
                .doOnNext(requestDTO -> log.info("Delete Office data Request Dto : {}", requestDTO))
                .flatMap(service::deleteAllOfficeDataForManagementProcessId)
                .doOnRequest(request -> log.debug("Request received to Delete : {}", request))
                .doOnSuccess(serverResponse -> log.info("Response for Delete Office Data : {}", serverResponse))
                .doOnError(err -> log.error("Error occurred while deleting office data : {}", err.getMessage()))
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(Mono.just(response), String.class)
                )
                .doOnSuccess(response -> log.info("Response for office data deleting : {}", response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest));
    }
}
