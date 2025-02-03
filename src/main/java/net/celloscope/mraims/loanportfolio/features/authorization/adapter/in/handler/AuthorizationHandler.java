package net.celloscope.mraims.loanportfolio.features.authorization.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.Constants;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.IAuthorizationUseCase;
import net.celloscope.mraims.loanportfolio.features.authorization.application.port.in.dto.request.AuthorizationRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationHandler {
    private final IAuthorizationUseCase useCase;

    public Mono<ServerResponse> gridViewOfAuthorization(ServerRequest serverRequest) {
        return this.buildAuthorizationRequest(serverRequest)
                .flatMap(useCase::gridViewOfAuthorization)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> tabViewOfAuthorization(ServerRequest serverRequest) {
        return this.buildAuthorizationRequest(serverRequest)
                .flatMap(useCase::tabViewOfAuthorization)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> lockSamityListForAuthorization(ServerRequest serverRequest) {
        return this.buildAuthorizationRequestDTO(serverRequest)
                .flatMap(useCase::lockSamityListForAuthorization)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> unlockSamityListForAuthorization(ServerRequest serverRequest) {
        return this.buildAuthorizationRequestDTO(serverRequest)
                .flatMap(useCase::unlockSamityListForAuthorization)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> authorizeSamityList(ServerRequest serverRequest) {
        return this.buildAuthorizationRequestDTO(serverRequest)
                .flatMap(useCase::authorizeSamityList)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> rejectSamityList(ServerRequest serverRequest) {
        return this.buildAuthorizationRequestDTO(serverRequest)
                .flatMap(useCase::rejectSamityList)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> unauthorizeSamityList(ServerRequest serverRequest) {
        return this.buildAuthorizationRequestDTO(serverRequest)
                .flatMap(useCase::unauthorizeSamityList)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private Mono<AuthorizationRequestDTO> buildAuthorizationRequest(ServerRequest serverRequest) {
        return Mono.just(AuthorizationRequestDTO.builder()
                .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                .samityId(serverRequest.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(""))
                .fieldOfficerId(serverRequest.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""))
                .limit(Integer.valueOf(
                        serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                .offset(Integer.valueOf(
                        serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                .build());
    }

    private Mono<AuthorizationRequestDTO> buildAuthorizationRequestDTO(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(AuthorizationRequestDTO.class)
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest
                            .queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue())
                            .orElse(""));
                    requestDTO.setSource(Constants.SOURCE_APPLICATION.getValue());
                    return requestDTO;
                })
                .doOnNext(authorizationRequestDTO -> log.debug("Authorization Process RequestDTO: {}",
                        authorizationRequestDTO));
    }
}
