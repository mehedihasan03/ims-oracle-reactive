package net.celloscope.mraims.loanportfolio.features.monthendprocess.adapter.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.MonthEndProcessUseCase;
import net.celloscope.mraims.loanportfolio.features.monthendprocess.application.port.in.dto.request.MonthEndProcessRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
@RequiredArgsConstructor
public class MonthEndProcessHandler {

    private final MonthEndProcessUseCase useCase;

    public Mono<ServerResponse> gridViewOfMonthEndProcess(ServerRequest serverRequest) {
        return Mono.just(MonthEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .flatMap(useCase::gridViewOfMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfStatusOfMonthEndProcess(ServerRequest serverRequest) {
        return Mono.just(MonthEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .flatMap(useCase::gridViewOfStatusOfMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> runSamityStatusOfMonthEndProcess(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MonthEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .flatMap(useCase::runSamityStatusOfMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> retrySamityStatusOfMonthEndProcess(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MonthEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .flatMap(useCase::retrySamityStatusOfMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> retryAllSamityStatusOfMonthEndProcess(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MonthEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .flatMap(useCase::retryAllSamityStatusOfMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfAccountingOfMonthEndProcess(ServerRequest serverRequest) {
        return Mono.just(MonthEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .flatMap(useCase::gridViewOfAccountingOfMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> runAccountingOfMonthEndProcess(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MonthEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .flatMap(useCase::runAccountingOfMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> retryAccountingByTransactionCodeListOfMonthEndProcess(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MonthEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .flatMap(useCase::retryAccountingByTransactionCodeListOfMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> retryAllAccountingOfMonthEndProcess(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(MonthEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .flatMap(useCase::retryAllAccountingOfMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> revertMonthEndProcess(ServerRequest serverRequest) {
        return Mono.just(
                        MonthEndProcessRequestDTO.builder()
                                .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                                .build()
                )
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::revertMonthEndProcess)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }
}
