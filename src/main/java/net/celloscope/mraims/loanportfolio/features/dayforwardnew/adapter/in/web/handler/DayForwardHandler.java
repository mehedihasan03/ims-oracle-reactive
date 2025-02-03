package net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.adapter.in.web.dto.DayForwardProcessRequestDto;
import net.celloscope.mraims.loanportfolio.features.dayforwardnew.application.port.in.DayForwardProcessTrackerUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
public class DayForwardHandler {
    private final DayForwardProcessTrackerUseCase dayForwardProcessTrackerUseCase;

    public DayForwardHandler(DayForwardProcessTrackerUseCase dayForwardProcessTrackerUseCase) {
        this.dayForwardProcessTrackerUseCase = dayForwardProcessTrackerUseCase;
    }

    public Mono<ServerResponse> dayForwardRoutineForOffice(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(DayForwardProcessRequestDto.class)
                .map(requestDTO -> {
                    requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElseThrow(() -> new ServerWebInputException("MFI ID is required.")));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID is required.")));
                    return requestDTO;
                })
                .flatMap(dayForwardProcessTrackerUseCase::dayForwardProcessV2)
                .flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(processTrackerResponseDTOMono))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfForwardDayRoutineForOffice(ServerRequest serverRequest) {
        return Mono.just(DayForwardProcessRequestDto.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(dayForwardProcessTrackerUseCase::refreshDayForwardProcess)
                .flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(processTrackerResponseDTOMono))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> resetDayForwardProcessForOffice(ServerRequest serverRequest) {
        return Mono.just(DayForwardProcessRequestDto.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .build())
                .filter(requestDTO -> !HelperUtil.checkIfNullOrEmpty(requestDTO.getOfficeId()))
                .flatMap(dayForwardProcessRequestDto -> dayForwardProcessTrackerUseCase.resetDayForwardProcessByOfficeId(dayForwardProcessRequestDto.getOfficeId()))
                .flatMap(string -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(string))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> confirmDayForwardRoutine(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(DayForwardProcessRequestDto.class)
                .map(requestDTO -> {
                    requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElseThrow(() -> new ServerWebInputException("MFI ID is required.")));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID is required.")));
                    return requestDTO;
                })
                .flatMap(dayForwardProcessTrackerUseCase::confirmDayForwardProcess)
                .flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(processTrackerResponseDTOMono))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> retryDayForwardRoutineForOffice(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(DayForwardProcessRequestDto.class)
                .map(requestDTO -> {
                    requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElseThrow(() -> new ServerWebInputException("MFI ID is required.")));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID is required.")));
                    return requestDTO;
                })
                .flatMap(dayForwardProcessTrackerUseCase::retryDayForwardProcess)
                .flatMap(processTrackerResponseDTOMono -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(processTrackerResponseDTOMono))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }
}
