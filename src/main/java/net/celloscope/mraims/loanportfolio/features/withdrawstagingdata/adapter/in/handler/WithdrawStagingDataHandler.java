package net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.IWithdrawStagingDataUseCase;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawPaymentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawStagingDataQueryDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawStagingDataHandler {

    private final IWithdrawStagingDataUseCase useCase;


    public Mono<ServerResponse> gridViewWithdrawStagingDataByOfficeV1(ServerRequest request) {
        log.info("Request Landed on gridViewOfWithdrawStagingDataForAuthorizationByOffice");
        return useCase.gridViewOfWithdrawStagingDataByOfficeV1(this.buildWithdrawStagingDataQueryDTO(request))
                .flatMap(withdrawGridViewByOfficeResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(withdrawGridViewByOfficeResponseDTO));
    }

    public Mono<ServerResponse> gridViewOfWithdrawStagingDataForAuthorizationByOffice(ServerRequest request) {
        log.info("Request Landed on gridViewOfWithdrawStagingDataForAuthorizationByOffice");
        return useCase.gridViewOfWithdrawStagingDataForAuthorizationByOffice(this.buildWithdrawStagingDataQueryDTO(request))
                .flatMap(authorizationWithdrawGridViewResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(authorizationWithdrawGridViewResponseDTO));
    }

    public Mono<ServerResponse> gridViewWithdrawStagingDataByFieldOfficerV1(ServerRequest request) {
        return useCase.gridViewOfWithdrawStagingDataByFieldOfficerV1(this.buildWithdrawStagingDataQueryDTO(request))
                .flatMap(withdrawStagingDataGridViewResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(withdrawStagingDataGridViewResponseDTO));
    }

    public Mono<ServerResponse> detailViewOfWithdrawStagingDataBySamityId(ServerRequest request) {
        return useCase.detailViewOfWithdrawStagingDataBySamityId(this.buildWithdrawStagingDataQueryDTO(request))
                .flatMap(withdrawDetailViewResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(withdrawDetailViewResponseDTO));
    }

    public Mono<ServerResponse> detailViewOfWithdrawStagingDataByMemberId(ServerRequest request) {
        return useCase.detailViewOfWithdrawStagingDataByMemberId(this.buildWithdrawStagingDataQueryDTO(request))
                .flatMap(withdrawDetailViewResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(withdrawDetailViewResponseDTO));
    }

    public Mono<ServerResponse> detailViewOfWithdrawStagingDataBySavingsAccountId(ServerRequest request) {
        return useCase.detailViewOfWithdrawStagingDataByAccountId(this.buildWithdrawStagingDataQueryDTO(request))
                .flatMap(withdrawDetailViewResponseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(withdrawDetailViewResponseDTO));
    }

    public Mono<ServerResponse> gridViewOfWithdrawStagingDataByOffice(ServerRequest serverRequest) {
        return useCase.gridViewOfWithdrawStagingDataByOffice(this.buildWithdrawStagingDataQueryDTO(serverRequest))
                .flatMap(responseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDTO)
                )
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfWithdrawCollectionData(ServerRequest serverRequest) {
        return useCase.withdrawCollectionGridView(buildWithdrawCollectionDataRequestDTO(serverRequest))
                .flatMap(responseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDTO))
                .doOnRequest(req -> log.info("Request Received for get withdraw collection data grid view : {}", req))
                .doOnSuccess(res -> log.info("Successfully get withdraw collection data grid view : {}", res))
                .doOnError(err -> log.info("Error occurred while getting withdraw collection data grid view : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private WithdrawStagingDataQueryDTO buildWithdrawCollectionDataRequestDTO(ServerRequest request) {
        return WithdrawStagingDataQueryDTO.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")))
                .fieldOfficerId(request.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                .build();
    }

    public Mono<ServerResponse> gridViewOfWithdrawStagingDataByFieldOfficer(ServerRequest serverRequest) {
        return useCase.gridViewOfWithdrawStagingDataByFieldOfficer(this.buildWithdrawStagingDataQueryDTO(serverRequest))
                .flatMap(responseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDTO)
                )
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    //    build requestDTO from query params
    private WithdrawStagingDataQueryDTO buildWithdrawStagingDataQueryDTO(ServerRequest request) {
        return WithdrawStagingDataQueryDTO.builder()
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElse(""))
                .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                .fieldOfficerId(request.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(""))
                .memberId(request.queryParam(QueryParams.MEMBER_ID.getValue()).orElse(""))
                .accountId(request.queryParam(QueryParams.ACCOUNT_ID.getValue()).orElse(""))
                .mfiId(request.queryParam(QueryParams.MFI_ID.getValue()).orElse(""))
                .loginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")))
                .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                .build();
    }

//    Process Management v2
    public Mono<ServerResponse> withdrawPayment(ServerRequest serverRequest) {
        return buildWithdrawPaymentRequestDto(serverRequest)
                .flatMap(useCase::withdrawPayment)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> updateWithdrawPayment(ServerRequest serverRequest) {
        return buildWithdrawPaymentRequestDto(serverRequest)
                .flatMap(useCase::updateWithdrawPayment)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private static Mono<WithdrawPaymentRequestDTO> buildWithdrawPaymentRequestDto(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(WithdrawPaymentRequestDTO.class)
                .map(request -> {
                    request.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    request.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return request;
                })
                .doOnNext(withdrawPaymentRequestDTO -> log.info("withdrawPaymentRequestDTO: {}", withdrawPaymentRequestDTO));
    }

    public Mono<ServerResponse> submitWithdrawPayment(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(WithdrawStagingDataQueryDTO.class)
                .map(request -> {
                    request.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    request.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return request;
                })
                .doOnNext(queryDTO -> log.info("withdrawRequestDTO: {}", queryDTO))
                .flatMap(useCase::submitWithdrawPayment)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> DetailViewOfWithdrawCollectionData(ServerRequest request) {
        return useCase.withdrawCollectionDetailView(buildDetailViewRequest(request))
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for get withdraw collection data detail view : {}", req))
                .doOnSuccess(res -> log.info("Successfully get withdraw collection data detail view : {}", res))
                .doOnError(err -> log.info("Error occurred while getting withdraw collection data detail view : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private WithdrawStagingDataQueryDTO buildDetailViewRequest(ServerRequest request) {
        return WithdrawStagingDataQueryDTO.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .id(request.queryParam(QueryParams.ID.getValue()).orElseThrow(() -> new ServerWebInputException("Id cannot be empty")))
                .build();
    }

    public Mono<ServerResponse> submitWithdrawData(ServerRequest request) {
        return request.bodyToMono(WithdrawEntitySubmitRequestDto.class)
                .map(dto -> {
                    dto.setMfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()));
                    dto.setLoginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    dto.setInstituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
                    dto.setOfficeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    dto.setUserRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()));
                    return dto;
                })
                .flatMap(useCase::submitWithdrawDataEntity)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(req -> log.info("Request Received for submit withdraw entity: {}", req))
                .doOnSuccess(res -> log.info("Successfully submit withdraw entity: {}", res))
                .doOnError(err -> log.info("Error occurred while processing submit withdraw request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> deleteWithdrawData(ServerRequest request) {
        return useCase.deleteWithdrawData(buildDeleteWithdrawStagingDataRequest(request))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(req -> log.info("Request Received for delete withdraw data: {}", req))
                .doOnSuccess(res -> log.info("Successfully delete withdraw data: {}", res))
                .doOnError(err -> log.info("Error occurred while processing delete withdraw data request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private WithdrawStagingDataQueryDTO buildDeleteWithdrawStagingDataRequest(ServerRequest request) {
        return WithdrawStagingDataQueryDTO.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .id(request.queryParam(QueryParams.ID.getValue()).orElseThrow(() -> new ServerWebInputException("Id cannot be empty")))
                .build();
    }
}
