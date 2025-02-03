package net.celloscope.mraims.loanportfolio.features.welfarefund.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.WelfareFundUseCase;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.request.WelfareFundRequestBody;
import net.celloscope.mraims.loanportfolio.features.welfarefund.application.port.in.dto.request.WelfareFundRequestDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelFareFundHandler {

    private final WelfareFundUseCase useCase;

    public Mono<ServerResponse> gridViewWelfareFundByOffice(ServerRequest serverRequest) {
        log.info("Request Landed on gridViewOfWelfareFundByOffice");
        return useCase.gridViewOfWelfareFundByOffice(this.buildWelfareFundRequestDTO(serverRequest))
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnRequest(req -> log.info("Request Received for Welfare fund grid view list : {}", req))
                .doOnSuccess(res -> log.info("Successfully retrieve Welfare fund grid view list : {}", res))
                .doOnError(err -> log.info("Error occurred while getting welfare fund grid view list : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> welfareFundDetailView(ServerRequest serverRequest) {
        log.info("Request Landed on welfareFundDetailView");
        return useCase.getWelfareFundDetailView(this.buildWelfareFundRequestDTO(serverRequest))
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnRequest(req -> log.info("Request Received for Welfare fund data : {}", req))
                .doOnSuccess(res -> log.info("Successfully retrieve Welfare fund data : {}", res))
                .doOnError(err -> log.info("Error occurred while getting welfare fund data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> loanAccountForWelfareByAccountId(ServerRequest serverRequest) {
        log.info("Request Landed on loanAccountDetailsByAccountId");
        return useCase.loanAccountDetailsByLoanAccountId(this.buildWelfareFundRequestDTO(serverRequest))
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnRequest(req -> log.info("Request Received for loan account details data : {}", req))
                .doOnSuccess(res -> log.info("Successfully retrieve loan account details data : {}", res))
                .doOnError(err -> log.info("Error occurred while getting loan account details data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private WelfareFundRequestDto buildWelfareFundRequestDTO(ServerRequest request) {
        return WelfareFundRequestDto.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElse(""))
                .loanAccountId(request.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse(""))
                .transactionDate(request.queryParam(QueryParams.TRANSACTION_DATE.getValue()).orElse(""))
                .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                .build();
    }

    public Mono<ServerResponse> saveCollectedWelfareFund(ServerRequest serverRequest) {
        log.info("Request Landed on saveCollectedWelfareFund");
        return serverRequest.bodyToMono(WelfareFundRequestDto.class)
                .flatMap(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(null));
                    if (requestDTO.getLoginId() == null) {
                        return Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "Login ID cannot be empty"));
                    }
                    return Mono.just(requestDTO);
                })
                .flatMap(useCase::saveCollectedWelfareFund)
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnRequest(req -> log.info("Request Received for saving collected welfare fund data : {}", req))
                .doOnSuccess(res -> log.info("Successfully saved collected welfare fund data : {}", res))
                .doOnError(err -> log.error("Error occurred while saving collected welfare fund data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> updateWelfareFundCollection(ServerRequest serverRequest) {
        log.info("Request Landed on updateWelfareFund");
        return buildSaveWelfareFundRequestDTO(serverRequest)
                .flatMap(useCase::updateCollectedWelfareFund)
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnRequest(req -> log.info("Request Received for updating collected welfare fund data : {}", req))
                .doOnSuccess(res -> log.info("Successfully updated collected welfare fund data : {}", res))
                .doOnError(err -> log.info("Error occurred while updating collected welfare fund data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getWelfareFundDetailsForLoanAccount(ServerRequest serverRequest) {
//        log.info("Request Landed on updateWelfareFund");
        return Mono.just(this.buildWelfareFundRequestDTO(serverRequest))
                .flatMap(useCase::getWelfareFundDetailsForLoanAccount)
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnRequest(req -> log.info("Request Received for History of welfare fund data by loan Account: {}", req))
                .doOnSuccess(res -> log.info("Successfully Retrieved history welfare fund data : {}", res))
                .doOnError(err -> log.info("Error occurred while retrieving history of welfare fund data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> authorizeWelfareFundDataByLoanAccountId(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(WelfareFundRequestDto.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    return requestDTO;
                })
                .flatMap(useCase::authorizeWelfareFundDataByLoanAccountId)
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnSuccess(res -> log.info("Successfully Authorized collected welfare fund data : {}", res))
                .doOnError(err -> log.info("Error occurred while Authorizing collected welfare fund data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> rejectWelfareFundDataByLoanAccountId(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(WelfareFundRequestDto.class)
                .map(requestDTO -> {
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    return requestDTO;
                })
                .flatMap(useCase::rejectWelfareFundDataByLoanAccountId)
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnSuccess(res -> log.info("Successfully Authorized collected welfare fund data : {}", res))
                .doOnError(err -> log.info("Error occurred while Authorizing collected welfare fund data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> gridViewOfWelfareFundDataByOfficeForAuthorization(ServerRequest serverRequest) {
        return Mono.just(WelfareFundRequestDto.builder().build())
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    return requestDTO;
                })
                .flatMap(useCase::gridViewOfWelfareFundDataByOfficeForAuthorization)
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnSuccess(res -> log.info("Authorization Grid View of welfare fund data : {}", res))
                .doOnError(err -> log.info("Error occurred Authorization Grid View of welfare fund data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> detailViewOfWelfareFundDataByLoanAccountForAuthorization(ServerRequest serverRequest) {
        return Mono.just(WelfareFundRequestDto.builder().build())
                .map(requestDTO -> {
                    requestDTO.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    requestDTO.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login ID cannot be empty")));
                    requestDTO.setLoanAccountId(serverRequest.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse(""));
                    requestDTO.setTransactionDate(serverRequest.queryParam(QueryParams.TRANSACTION_DATE.getValue()).orElse(""));
                    return requestDTO;
                })
                .flatMap(useCase::detailViewOfWelfareFundDataByLoanAccountForAuthorization)
                .flatMap(welfareFundResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(welfareFundResponseDto))
                .doOnSuccess(res -> log.info("Authorization detail View of welfare fund data : {}", res))
                .doOnError(err -> log.info("Authorization detail View of welfare fund data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private Mono<WelfareFundRequestDto> buildSaveWelfareFundRequestDTO(ServerRequest request) {
        return request.bodyToMono(WelfareFundRequestBody.class)
                .map(requestBody -> WelfareFundRequestDto.builder()
                        .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                        .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                        .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                        .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElse(""))
                        .loanAccountId(request.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse(""))
                        .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""))
                        .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                        .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                        .loanAccountId(requestBody.getLoanAccountId())
                        .amount(requestBody.getAmount())
                        .paymentMethod(requestBody.getPaymentMethod())
                        .referenceNo(requestBody.getReferenceNo())
                        .build());
    }

    public Mono<ServerResponse> resetWelfareFundData(ServerRequest serverRequest) {
            return serverRequest.bodyToMono(WelfareFundRequestDto.class)
                    .map(dto -> buildRequestDtoForResetWelfareFund(dto, serverRequest))
                    .flatMap(useCase::resetWelfareFundData)
                    .flatMap(submitRebateResponseDto -> ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(submitRebateResponseDto))
                    .doOnRequest(r -> log.info("Request Received for Reset Welfare Fund Data: {}", r))
                    .doOnSuccess(res -> log.info("Response for Reset Welfare Fund Data {}", res.statusCode()))
                    .doOnError(err -> log.error("Error occurred while processing reset Welfare Fund Request: {}", err.getMessage()))
                    .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                    .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private WelfareFundRequestDto buildRequestDtoForResetWelfareFund(WelfareFundRequestDto requestDto, ServerRequest serverRequest) {
        requestDto.setInstituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
        requestDto.setOfficeId(serverRequest.queryParam(OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")));
        requestDto.setLoginId(serverRequest.queryParam(LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login Id cannot be empty")));
        log.info("Request for Reset Loan Rebate: {}", requestDto);
        return requestDto;
    }
}
