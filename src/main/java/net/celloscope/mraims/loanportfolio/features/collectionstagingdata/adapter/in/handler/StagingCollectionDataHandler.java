package net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.adapter.in.collectionDto.CollectionStagingRequestDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.CollectionStagingDataQueryUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.PaymentCollectionUseCase;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.CollectionEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.collectionstagingdata.application.port.in.dto.commands.PaymentCollectionBySamityCommand;
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
public class StagingCollectionDataHandler {

    private final CollectionStagingDataQueryUseCase queryUseCase;
    private final PaymentCollectionUseCase paymentCollectionUseCase;

    public Mono<ServerResponse> gridViewOfRegularCollectionForOffice(ServerRequest serverRequest) {
        return Mono.just(serverRequest)
                .map(request -> CollectionDataRequestDTO.builder()
                        .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")))
                        .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .flatMap(queryUseCase::gridViewOfRegularCollectionForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfRegularCollectionForFieldOfficer(ServerRequest serverRequest) {
        return Mono.just(serverRequest)
                .map(request -> CollectionDataRequestDTO.builder()
                        .fieldOfficerId(request.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                        .loginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")))
                        .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .flatMap(queryUseCase::gridViewOfRegularCollectionForFieldOfficer)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfSpecialCollectionForOffice(ServerRequest serverRequest) {
        return Mono.just(serverRequest)
                .map(request -> CollectionDataRequestDTO.builder()
                        .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")))
                        .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .flatMap(queryUseCase::gridViewOfSpecialCollectionForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfSpecialCollectionForFieldOfficer(ServerRequest serverRequest) {
        return Mono.just(serverRequest)
                .map(request -> CollectionDataRequestDTO.builder()
                        .fieldOfficerId(request.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                        .loginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")))
                        .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .flatMap(queryUseCase::gridViewOfSpecialCollectionForFieldOfficer)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> listOfSpecialCollectionSamity(ServerRequest serverRequest) {
        return Mono.just(serverRequest)
                .map(request -> CollectionDataRequestDTO.builder()
                        .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .fieldOfficerId(request.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                        .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(""))
                        .loginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")))
                        .mfiId(request.queryParam(QueryParams.MFI_ID.getValue()).orElse(""))
                        .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .flatMap(queryUseCase::listOfSpecialCollectionSamity)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> collectPaymentBySamity(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(PaymentCollectionBySamityCommand.class)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return command;
                })
                .flatMap(paymentCollectionUseCase::collectPaymentBySamity)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnError(err -> log.error("Error on payment collection: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));

    }

    public Mono<ServerResponse> updateCollectionPaymentBySamity(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(PaymentCollectionBySamityCommand.class)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return command;
                })
                .flatMap(paymentCollectionUseCase::updateCollectionPaymentBySamity)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnError(err -> log.error("Error on Collection Payment Update: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> submitCollectionDataForAuthorizationBySamity(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CollectionDataRequestDTO.class)
                .map(command -> {
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return command;
                })
                .flatMap(queryUseCase::submitCollectionDataForAuthorizationBySamity)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getCollectionDataGridView(ServerRequest request) {
        return queryUseCase.getCollectionStagingGridView(buildWriteOffCollectionAccountDataRequest(request))
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for get collection data grid view : {}", req))
                .doOnSuccess(res -> log.info("Successfully get collection data grid view : {}", res))
                .doOnError(err -> log.info("Error occurred while getting collection data grid view : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> getCollectionDataDetailView(ServerRequest request) {
        return queryUseCase.getCollectionStagingDetailView(buildDetailViewRequest(request))
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for get collection data detail view : {}", req))
                .doOnSuccess(res -> log.info("Successfully get collection data detail view : {}", res))
                .doOnError(err -> log.info("Error occurred while getting collection data detail view : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private CollectionStagingRequestDto buildWriteOffCollectionAccountDataRequest(ServerRequest request) {
        return CollectionStagingRequestDto.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")))
                .collectionType(request.queryParam(QueryParams.COLLECTION_TYPE.getValue()).orElseThrow(() -> new ServerWebInputException("Collection Type cannot be empty")))
                .fieldOfficerId(request.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                .build();
    }

    private CollectionStagingRequestDto buildDetailViewRequest(ServerRequest request) {
        return CollectionStagingRequestDto.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .id(request.queryParam(QueryParams.ID.getValue()).orElseThrow(() -> new ServerWebInputException("Id cannot be empty")))
                .build();
    }

    public Mono<ServerResponse> editSpecialCollectionData(ServerRequest request) {
        return request.bodyToMono(CollectionStagingRequestDto.class)
                .map(dto ->{
                    dto.setMfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()));
                    dto.setUserRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()));
                    dto.setLoginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    dto.setInstituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
                    return dto;
                })
                .flatMap(queryUseCase::editSpecialCollectionData)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnError(err -> log.error("Error while edit collection data: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> submitCollectionDataForAuthorizationByOid(ServerRequest request) {
        String oid = request.queryParam("oid").orElse("");
        return request.bodyToMono(CollectionDataRequestDTO.class)
                .map(dto ->{
                    dto.setMfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()));
                    dto.setLoginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    dto.setOid(oid);
                    return dto;
                })
                .flatMap(queryUseCase::submitCollectionDataForAuthorizationByOid)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnError(err -> log.error("Error while submit collection data for authorization: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));

    }

    public Mono<ServerResponse> submitCollectionDataEntityByOidList(ServerRequest request) {
        return request.bodyToMono(CollectionEntitySubmitRequestDto.class)
                .map(dto ->{
                    dto.setMfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()));
                    dto.setLoginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    dto.setInstituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
                    dto.setOfficeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    dto.setUserRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()));
                    return dto;
                })
                .flatMap(queryUseCase::submitCollectionDataEntity)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnError(err -> log.error("Error while submit entity wise collection data : {}", err.getMessage()))
                .doOnRequest(req -> log.info("Request received for submit entity wise collection data : {}", req))
                .doOnSuccess(res -> log.info("Successfully submitted entity wise collection data : {}", res))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }
}
