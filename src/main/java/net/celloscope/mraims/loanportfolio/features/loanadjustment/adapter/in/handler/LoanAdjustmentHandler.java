package net.celloscope.mraims.loanportfolio.features.loanadjustment.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.LoanAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustmentEditRequestDto;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.AdjustmentEntitySubmitRequestDto;
import net.celloscope.mraims.loanportfolio.features.loanadjustment.application.port.in.dto.request.LoanAdjustmentRequestDTO;
import net.celloscope.mraims.loanportfolio.features.withdrawstagingdata.application.port.in.dto.queries.WithdrawStagingDataQueryDTO;
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
public class LoanAdjustmentHandler {
        private final LoanAdjustmentUseCase useCase;

        public Mono<ServerResponse> createLoanAdjustmentForMember(ServerRequest serverRequest) {
                return serverRequest.bodyToMono(LoanAdjustmentRequestDTO.class)
                                .map(requestDTO -> {
                                        requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                                        requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                                        requestDTO.setSamityId(serverRequest.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(""));
                                        return requestDTO;
                                })
                                .flatMap(useCase::createLoanAdjustmentForMember)
                                .flatMap(response -> ServerResponse.ok()
                                                .bodyValue(response))
                                .onErrorResume(ExceptionHandlerUtil.class,
                                                e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                                                e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
        }

        public Mono<ServerResponse> authorizeLoanAdjustment(ServerRequest serverRequest) {
                return serverRequest.bodyToMono(LoanAdjustmentRequestDTO.class)
                                .map(requestDTO -> {
                                        requestDTO.setMfiId(serverRequest.queryParam(QueryParams.MFI_ID.getValue())
                                                        .orElse(""));
                                        requestDTO.setOfficeId(serverRequest
                                                        .queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                                        requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue())
                                                .orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                                        return requestDTO;
                                })
                                .flatMap(useCase::authorizeLoanAdjustmentForSamity)
                                .flatMap(response -> ServerResponse.ok()
                                                .bodyValue(response))
                                .onErrorResume(ExceptionHandlerUtil.class,
                                                e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                                                e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
        }

        public Mono<ServerResponse> submitLoanAdjustment(ServerRequest serverRequest) {
                return serverRequest.bodyToMono(LoanAdjustmentRequestDTO.class)
                                .map(requestDTO -> {
                                        requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue())
                                                .orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                                        return requestDTO;
                                })
                                .flatMap(useCase::submitLoanAdjustmentDataForAuthorization)
                                .flatMap(response -> ServerResponse.ok()
                                                .bodyValue(response))
                                .onErrorResume(ExceptionHandlerUtil.class,
                                                e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                                                e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
        }

    public Mono<ServerResponse> gridViewOfLoanAdjustmentForOffice(ServerRequest serverRequest) {
        return Mono.just(LoanAdjustmentRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .loginId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")))
                        .limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .flatMap(useCase::gridViewOfLoanAdjustmentForOffice)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }


    public Mono<ServerResponse> gridViewOfLoanAdjustmentForFieldOfficer(ServerRequest serverRequest) {
        return Mono.just(LoanAdjustmentRequestDTO.builder()
                        .officeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .fieldOfficerId(serverRequest.queryParam(QueryParams.FIELD_OFFICER_ID.getValue()).orElse(""))
                        .limit(Integer.valueOf(serverRequest.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(serverRequest.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .build())
                .doOnNext(requestDTO -> log.info("Request DTO: {}", requestDTO))
                .flatMap(useCase::gridViewOfLoanAdjustmentForFieldOfficer)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> detailViewOfLoanAdjustmentForSamity(ServerRequest serverRequest) {
        return Mono.just(LoanAdjustmentRequestDTO.builder()
                        .samityId(serverRequest.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(""))
                        .build())
                .flatMap(useCase::detailViewOfLoanAdjustmentForSamity)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> detailViewOfLoanAdjustmentForMember(ServerRequest serverRequest) {
        return Mono.just(LoanAdjustmentRequestDTO.builder()
                        .memberId(serverRequest.queryParam(QueryParams.MEMBER_ID.getValue()).orElse(""))
                        .build())
                .flatMap(useCase::detailViewOfLoanAdjustmentForAMember)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> detailsOfLoanAdjustmentCreationForAMember(ServerRequest serverRequest) {
        return Mono.just(LoanAdjustmentRequestDTO.builder()
                        .memberId(serverRequest.queryParam(QueryParams.MEMBER_ID.getValue()).orElse(""))
                        .build())
                .flatMap(useCase::detailsOfLoanAdjustmentCreationForAMember)
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> loanAdjustmentGridView(ServerRequest request) {
        return useCase.AdjustmentCollectionGridView(buildAdjustmentCollectionDataRequestDTO(request))
                .flatMap(responseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDTO))
                .doOnRequest(req -> log.info("Request Received for get adjustment collection data grid view : {}", req))
                .doOnSuccess(res -> log.info("Successfully get adjustment data grid view : {}", res))
                .doOnError(err -> log.info("Error occurred while getting adjustment collection data grid view : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private LoanAdjustmentRequestDTO buildAdjustmentCollectionDataRequestDTO(ServerRequest request) {
        return LoanAdjustmentRequestDTO.builder()
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

    public Mono<ServerResponse> loanAdjustmentDetailView(ServerRequest request) {
        return useCase.AdjustmentCollectionDetailView(buildDetailViewRequest(request))
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for get adjustment collection data detail view : {}", req))
                .doOnSuccess(res -> log.info("Successfully get adjustment collection data detail view : {}", res))
                .doOnError(err -> log.info("Error occurred while getting adjustment collection data detail view : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private LoanAdjustmentRequestDTO buildDetailViewRequest(ServerRequest request) {
        return LoanAdjustmentRequestDTO.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .id(request.queryParam(QueryParams.ID.getValue()).orElseThrow(() -> new ServerWebInputException("Id cannot be empty")))
                .build();
    }

    public Mono<ServerResponse> updateAdjustmentAmount(ServerRequest request) {
        return request
                .bodyToMono(AdjustmentEditRequestDto.class)
                .map(withdrawRequestDto -> {
                    withdrawRequestDto.setMfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()));
                    withdrawRequestDto.setUserRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()));
                    withdrawRequestDto.setInstituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
                    withdrawRequestDto.setLoginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login id cannot be empty")));
                    return withdrawRequestDto;
                })
                .flatMap(useCase::updateAdjustmentAmount)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnNext(req -> log.info("Request for edit adjustment amount: {}", req))
                .doOnRequest(req -> log.info("Request Received for edit adjustment amount: {}", req))
                .doOnSuccess(res -> log.info("Successfully edit adjustment amount: {}", res))
                .doOnError(err -> log.info("Error occurred while processing edit adjustment request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> submitLoanAdjustmentEntity(ServerRequest request) {
        return request.bodyToMono(AdjustmentEntitySubmitRequestDto.class)
                .map(dto -> {
                    dto.setMfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()));
                    dto.setLoginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    dto.setInstituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
                    dto.setOfficeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    dto.setUserRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()));
                    return dto;
                })
                .flatMap(useCase::submitAdjustmentDataEntity)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(req -> log.info("Request Received for submit adjustment entity: {}", req))
                .doOnSuccess(res -> log.info("Successfully submit adjustment entity: {}", res))
                .doOnError(err -> log.info("Error occurred while processing submit adjustment request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }


    public Mono<ServerResponse> deleteAdjustmentData(ServerRequest request) {
        return useCase.resetLoanAdjustmentDataByEntity(buildDeleteLoanAdjustmentDataRequest(request))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(req -> log.info("Request Received for delete adjustment data: {}", req))
                .doOnSuccess(res -> log.info("Successfully deleted adjustment data: {}", res))
                .doOnError(err -> log.info("Error occurred while processing delete adjustment data request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private LoanAdjustmentRequestDTO buildDeleteLoanAdjustmentDataRequest(ServerRequest request) {
        return LoanAdjustmentRequestDTO.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .id(request.queryParam(QueryParams.ID.getValue()).orElseThrow(() -> new ServerWebInputException("Id cannot be empty")))
                .build();
    }
}
