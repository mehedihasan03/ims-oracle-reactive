package net.celloscope.mraims.loanportfolio.features.dayendprocess.adapter.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.DayEndProcessTrackerUseCase;
import net.celloscope.mraims.loanportfolio.features.dayendprocess.application.port.in.dto.request.DayEndProcessRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
@RequiredArgsConstructor
public class DayEndProcessHandler {

    private final DayEndProcessTrackerUseCase useCase;


    public Mono<ServerResponse> gridViewOfDayEndProcessForOffice(ServerRequest serverRequest) {
        return Mono.just(DayEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .build())
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::gridViewOfDayEndProcessForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> detailViewOfDayEndProcessForOffice(ServerRequest serverRequest) {
        return Mono.just(DayEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .build())
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::detailViewOfDayEndProcessForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> runDayEndProcessForOffice(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(DayEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::runDayEndProcessForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> retryDayEndProcessForOffice(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(DayEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::retryDayEndProcessForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }
    public Mono<ServerResponse> retryAllDayEndProcessForOffice(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(DayEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::retryAllDayEndProcessForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> generateAutoVoucherForDayEndProcess(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(DayEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::generateAutoVoucherForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> retryAutoVoucherGenerationForDayEndProcess(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(DayEndProcessRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return requestDTO;
                })
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::retryAutoVoucherGenerationForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> deleteAutoVoucherGenerationForOffice(ServerRequest serverRequest) {
        return Mono.just(DayEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .isScheduledRequest(false)
                        .build())
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::deleteAutoVoucherGenerationForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> deleteDayEndProcessForOffice(ServerRequest serverRequest) {
        return Mono.just(DayEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .build())
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::deleteDayEndProcessForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> revertDayEndProcessForAIS(ServerRequest serverRequest) {
        return Mono.just(DayEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .build())
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::revertDayEndProcessByAISForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> revertDayEndProcessForMIS(ServerRequest serverRequest) {
        return Mono.just(DayEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .build())
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::revertDayEndProcessByMISForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> getStatusOfDayEndProcessForOffice(ServerRequest serverRequest) {
        return Mono.just(DayEndProcessRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                        .build())
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(useCase::getStatusOfDayEndProcessForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }
}
