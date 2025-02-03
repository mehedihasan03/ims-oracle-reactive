package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.queries.CollectionDataRequestDTO;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
@RequiredArgsConstructor
public class CollectionStagingDataHandler {
        private final CollectionStagingDataQueryUseCase useCase;

//    Regular Collection Grid View
    public Mono<ServerResponse> gridViewOfRegularCollectionByOffice(ServerRequest serverRequest) {
        return useCase
                .gridViewOfRegularCollectionByOffice(buildRequestForGridView(serverRequest))
                .doOnRequest(r -> log.info("---"))
                .flatMap(collectionGridViewByOfficeResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(collectionGridViewByOfficeResponseDTO)
                )
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfRegularCollectionByFieldOfficer(ServerRequest serverRequest) {
        return useCase
                .gridViewOfRegularCollectionByFieldOfficer(buildRequestForGridView(serverRequest))
                .doOnRequest(r -> log.info("---"))
                .flatMap(collectionStagingDataGridViewResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(collectionStagingDataGridViewResponse)
                )
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

//    Special Collection Grid View
    public Mono<ServerResponse> gridViewOfSpecialCollectionByOffice(ServerRequest serverRequest) {
        return useCase
                .gridViewOfSpecialCollectionByOffice(buildRequestForGridView(serverRequest))
                .doOnRequest(r -> log.info("---"))
                .flatMap(collectionGridViewByOfficeResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(collectionGridViewByOfficeResponseDTO)
                )
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfSpecialCollectionByFieldOfficer(ServerRequest serverRequest) {
        return useCase
                .gridViewOfSpecialCollectionByFieldOfficer(buildRequestForGridView(serverRequest))
                .doOnRequest(r -> log.info("---"))
                .flatMap(collectionStagingDataGridViewResponse -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(collectionStagingDataGridViewResponse)
                )
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

//    Authorization Collection Grid View
    public Mono<ServerResponse> gridViewOfRegularCollectionAuthorizationByOffice(ServerRequest serverRequest) {
        return useCase
                .gridViewOfRegularCollectionAuthorizationByOffice(this.buildRequestForGridView(serverRequest))
                .flatMap(authorizationGridViewResponseDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(authorizationGridViewResponseDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfSpecialCollectionAuthorizationByOffice(ServerRequest serverRequest) {
        return useCase
                .gridViewOfSpecialCollectionAuthorizationByOffice(this.buildRequestForGridView(serverRequest))
                .flatMap(authorizationGridViewResponseDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(authorizationGridViewResponseDTO))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


//    Detail View Of Collection
    public Mono<ServerResponse> getCollectionStagingDataDetailViewBySamity(ServerRequest request) {
        CollectionDataRequestDTO requestDTO = this.buildRequestForGridView(request);
        return useCase.getCollectionStagingDataDetailViewBySamityId(requestDTO)
                .doOnRequest(r -> log.info("---"))
                .flatMap(response -> {
                    response.setOfficeId(requestDTO.getOfficeId());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> getCollectionStagingDataDetailViewByAccount(ServerRequest request) {
        CollectionDataRequestDTO requestDTO = this.buildRequestForGridView(request);
        return useCase.getCollectionStagingDataDetailViewByAccountId(requestDTO)
                .doOnRequest(r -> log.info("---"))
                .flatMap(response -> {
                    response.setOfficeId(requestDTO.getOfficeId());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> getCollectionStagingDataDetailViewByMemberId(ServerRequest request) {
        CollectionDataRequestDTO requestDTO = this.buildRequestForGridView(request);
        return useCase.getCollectionStagingDataDetailViewByMemberId(requestDTO)
                .flatMap(response -> {
                    response.setOfficeId(requestDTO.getOfficeId());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> getCollectionDetailViewByFieldOfficer(ServerRequest request) {
        CollectionDataRequestDTO requestDTO = this.buildRequestForGridView(request);
        return useCase.getCollectionDetailViewByFieldOfficer(requestDTO)
                .flatMap(response -> {
                    response.setOfficeId(requestDTO.getOfficeId());
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> editCommitForCollectionDataBySamity(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CollectionDataRequestDTO.class)
                .map(request -> {
                    request.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    return request;
                })
                .flatMap(useCase::editCommitForCollectionDataBySamity)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.debug("Request Received for commit collection data: {}", r))
                .doOnSuccess(res -> log.info("Response for commit collection: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing commit collection request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    private CollectionDataRequestDTO buildRequestForGridView(ServerRequest serverRequest) {
        return CollectionDataRequestDTO
                .builder()
                .fieldOfficerId(serverRequest.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                .mfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElse(""))
                .loginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")))
                .samityId(serverRequest.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(""))
                .accountId(serverRequest.queryParam(QueryParams.ACCOUNT_ID.getValue()).orElse(""))
                .memberId(serverRequest.queryParam(QueryParams.MEMBER_ID.getValue()).orElse(""))
                .limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                .offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                .build();
    }
}
