package net.celloscope.mraims.loanportfolio.features.cancel.adapter.in;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.CancelledSamityUseCase;
import net.celloscope.mraims.loanportfolio.features.cancel.application.port.in.request.CancelSamityRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
public class CancelSamityHandler {

    private final CancelledSamityUseCase samityUseCase;
    public CancelSamityHandler( CancelledSamityUseCase samityUseCase) {
        this.samityUseCase = samityUseCase;
    }

    public Mono<ServerResponse> cancelSamity(ServerRequest serverRequest){
        return serverRequest.bodyToMono(CancelSamityRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setInstituteOid(serverRequest.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElse(""));
                    requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setManagementProcessId(serverRequest.queryParam(QueryParams.MANAGEMENT_PROCESS_ID.getValue()).orElse(""));
                    return  requestDTO;
                })
                .doOnNext(requestDTO -> log.info("Cancel Samity by Samity Id : {}",requestDTO))
                .flatMap(samityUseCase::cancelSamityBySamityId)
                .doOnRequest(request -> log.debug("Request received to cancel samity : {}",request))
                .doOnSuccess(serverResponse -> log.info("Response for samity cancellation : {}",serverResponse))
                .doOnError(err -> log.error("Error occurred while cancelling samity : {}", err.getMessage()))
                .flatMap(responseDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));

    }

    public Mono<ServerResponse> restoreCancelledSamity(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CancelSamityRequestDTO.class)
                .map(requestDTO -> buildRequestForRestoreSamity(serverRequest, requestDTO))
                .doOnNext(requestDTO -> log.info("Restore Cancelled Samity by Samity Id : {}", requestDTO))
                .flatMap(samityUseCase::restoreCancelledSamityBySamityId)
                .doOnRequest(request -> log.debug("Request received to restore cancelled samity : {}", request))
                .doOnSuccess(serverResponse -> log.info("Response for samity restoration : {}", serverResponse))
                .doOnError(err -> log.error("Error occurred while restoring cancelled samity : {}", err.getMessage()))
                .flatMap(responseDTO -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private CancelSamityRequestDTO buildRequestForRestoreSamity(ServerRequest serverRequest, CancelSamityRequestDTO requestDTO) {
        requestDTO.setInstituteOid(serverRequest.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute OID can not be empty")));
        requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""));
        requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
        requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office ID can not be empty")));
        requestDTO.setManagementProcessId(serverRequest.queryParam(QueryParams.MANAGEMENT_PROCESS_ID.getValue()).orElse(""));
        return requestDTO;
    }

}
