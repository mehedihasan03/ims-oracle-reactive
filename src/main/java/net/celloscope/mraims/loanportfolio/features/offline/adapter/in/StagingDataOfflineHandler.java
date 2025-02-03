package net.celloscope.mraims.loanportfolio.features.offline.adapter.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.offline.application.port.StagingDataOfflineUseCase;
import net.celloscope.mraims.loanportfolio.features.offline.application.port.dto.request.StagingDataOfflineRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class StagingDataOfflineHandler {

    private final StagingDataOfflineUseCase useCase;

    public Mono<ServerResponse> downloadStagingDataByFieldOfficer(ServerRequest serverRequest){
        return Mono.just(StagingDataOfflineRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .fieldOfficerId(serverRequest.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .build())
                .flatMap(useCase::downloadStagingDataByFieldOfficer)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()));
    }

    public Mono<ServerResponse> deleteDownloadedStagingDataByFieldOfficer(ServerRequest serverRequest){
        return Mono.just(StagingDataOfflineRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .fieldOfficerId(serverRequest.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .build())
                .flatMap(useCase::deleteDownloadedStagingDataByFieldOfficer)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()));
    }
}
