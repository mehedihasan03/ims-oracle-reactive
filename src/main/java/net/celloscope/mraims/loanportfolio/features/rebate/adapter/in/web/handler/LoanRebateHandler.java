package net.celloscope.mraims.loanportfolio.features.rebate.adapter.in.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.LoanRebateUseCase;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.*;

@Component
@Slf4j
public class LoanRebateHandler {

    private final LoanRebateUseCase loanRebateUseCase;

    public LoanRebateHandler(LoanRebateUseCase loanRebateUseCase) {
        this.loanRebateUseCase = loanRebateUseCase;
    }

    public Mono<ServerResponse> getLoanRebate(ServerRequest serverRequest)  {
        return loanRebateUseCase
                .getLoanRebateInfoByLoanAccountId(serverRequest)
                .flatMap(loanRebateResponseDTO ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loanRebateResponseDTO))
                .doOnRequest(r -> log.info("Request Received for Loan Rebate: {}", r))
                .doOnSuccess(res -> log.info("Response for Loan Rebate: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing Loan Rebate Request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(throwable -> Mono.error(new ExceptionHandlerUtil()))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getAccountDetailsForRebate(ServerRequest serverRequest) {
        GetDetailsByMemberRequestDto requestDto = buildGetDetailsByMemberRequestDto(serverRequest);
        return loanRebateUseCase
                .collectAccountDetailsByMemberId(requestDto)
                .flatMap(getDetailsByMemberResponseDto ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(getDetailsByMemberResponseDto))
                .doOnRequest(r -> log.info("Request received for getting account details for Rebate: {}", requestDto))
                .doOnSuccess(res -> log.info("Response for account details for Rebate: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing account details by member for Rebate: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> settleRebate(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SettleRebateRequestDto.class)
                .map(settleRebateRequestDto -> buildSettleRebateRequest(serverRequest, settleRebateRequestDto))
                .flatMap(loanRebateUseCase::settleRebate)
                .flatMap(settleRebateResponseDto -> ServerResponse.ok().bodyValue(settleRebateResponseDto))
                .doOnRequest(r -> log.info("Request received for settling Rebate: {}", r))
                .doOnSuccess(res -> log.info("Response for settling Rebate: {}", res.statusCode()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getLoanRebateDetail(ServerRequest serverRequest) {
        GetLoanRebateDetailRequestDto requestDto = buildRequestDtoForGettingDetailOfLoanReabte(serverRequest);
        return loanRebateUseCase
                .getLoanRebateDetail(requestDto)
                .flatMap(getLoanRebateDetailResponseDto ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(getLoanRebateDetailResponseDto))
                .doOnRequest(r -> log.info("Request Received for Loan Rebate Detail View: {}", r))
                .doOnSuccess(res -> log.info("Response for Loan Rebate Detail View: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing Loan Rebate Detail View Request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private GetLoanRebateDetailRequestDto buildRequestDtoForGettingDetailOfLoanReabte(ServerRequest serverRequest) {
        return GetLoanRebateDetailRequestDto.builder()
                .mfiId(serverRequest.headers().firstHeader(HeaderNames.MFI_ID.getValue()))
                .loginId(serverRequest.headers().firstHeader(HeaderNames.LOGIN_ID.getValue()))
                .instituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .id(serverRequest.queryParam(ID.getValue()).orElseThrow(() -> new ServerWebInputException("ID cannot be empty")))
                .build();
    }


    public Mono<ServerResponse> submitLoanRebate(ServerRequest serverRequest) {
        return
                serverRequest.bodyToMono(GetLoanRebateDetailRequestDto.class)
                        .map(requestDtoWithBody -> buildRequestDtoForSubmittingLoanRebate(requestDtoWithBody, serverRequest))
                        .flatMap(loanRebateUseCase::submitLoanRebate)
                .flatMap(submitRebateResponseDto ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(submitRebateResponseDto))
                .doOnRequest(r -> log.info("Request Received for Submitting Loan Rebate: {}", r))
                .doOnSuccess(res -> log.info("Response for Submitting Loan Rebate: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing Submitting Loan Rebate Request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> resetLoanRebate(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(GetLoanRebateDetailRequestDto.class)
                .map(dto -> buildRequestDtoForResetLoanRebate(dto, serverRequest))
                .flatMap(loanRebateUseCase::resetLoanRebate)
                .flatMap(submitRebateResponseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(submitRebateResponseDto))
                .doOnRequest(r -> log.info("Request Received for Reset Loan Rebate: {}", r))
                .doOnSuccess(res -> log.info("Response for Reset Loan Rebate: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing reset Loan Rebate Request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> updateLoanRebate(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SettleRebateRequestDto.class)
                        .map(requestDtoWithBody -> buildUpdateLoanRebateRequestDto(requestDtoWithBody, serverRequest))
                        .flatMap(loanRebateUseCase::updateLoanRebate)
                        .flatMap(updateRebateResponseDto ->
                                ServerResponse
                                        .ok()
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(updateRebateResponseDto))
                        .doOnRequest(r -> log.info("Request Received for Updating Loan Rebate: {}", r))
                        .doOnSuccess(res -> log.info("Response for Updating Loan Rebate: {}", res.statusCode()))
                        .doOnError(err -> log.error("Error occurred while processing Update Loan Rebate Request: {}", err.getMessage()))
                        .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                        .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private SettleRebateRequestDto buildUpdateLoanRebateRequestDto(SettleRebateRequestDto requestDtoWithBody, ServerRequest serverRequest) {
        requestDtoWithBody.setLoginId(serverRequest.headers().firstHeader(HeaderNames.LOGIN_ID.getValue()));
        requestDtoWithBody.setMfiId(serverRequest.headers().firstHeader(HeaderNames.MFI_ID.getValue()));
        requestDtoWithBody.setInstituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
        requestDtoWithBody.setOfficeId(serverRequest.queryParam(OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")));
        return requestDtoWithBody;
    }

    private GetLoanRebateDetailRequestDto buildRequestDtoForSubmittingLoanRebate(GetLoanRebateDetailRequestDto requestDto, ServerRequest serverRequest) {
        requestDto.setLoginId(serverRequest.headers().firstHeader(HeaderNames.LOGIN_ID.getValue()));
        requestDto.setMfiId(serverRequest.headers().firstHeader(HeaderNames.MFI_ID.getValue()));
        requestDto.setInstituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
        requestDto.setOfficeId(serverRequest.queryParam(OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")));
        return requestDto;
    }

    private GetLoanRebateDetailRequestDto buildRequestDtoForResetLoanRebate(GetLoanRebateDetailRequestDto requestDto, ServerRequest serverRequest) {
        requestDto.setInstituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
        requestDto.setOfficeId(serverRequest.queryParam(OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")));
        requestDto.setLoginId(serverRequest.queryParam(LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login Id cannot be empty")));
        log.info("Request for Reset Loan Rebate: {}", requestDto);
        return requestDto;
    }


    public Mono<ServerResponse> loanRebateGridViewByOffice(ServerRequest serverRequest) {
        LoanRebateGridViewByOfficeRequestDto requestDto = buildLoanrebateGridViewByOfficeRequestDto(serverRequest);
        return loanRebateUseCase
                .getLoanRebateGridViewByOfficeId(requestDto)
                .switchIfEmpty(Mono.just(LoanRebateGridViewByOfficeResponseDto.builder()
                        .data(new ArrayList<>())
                        .totalCount(0)
                        .build()))
                .flatMap(loanRebateGridViewByOfficeResponseDto ->
                        ServerResponse
                                .ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(loanRebateGridViewByOfficeResponseDto))
                .doOnRequest(r -> log.info("Request Received for Loan Rebate Grid View By Office: {}", requestDto))
                .doOnSuccess(res -> log.info("Response for Loan Rebate Grid View By Office: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing Loan Rebate Grid View By Office Request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private LoanRebateGridViewByOfficeRequestDto buildLoanrebateGridViewByOfficeRequestDto(ServerRequest serverRequest) {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LoanRebateGridViewByOfficeRequestDto.builder()
                .mfiId(serverRequest.headers().firstHeader(HeaderNames.MFI_ID.getValue()))
                .loginId(serverRequest.headers().firstHeader(HeaderNames.LOGIN_ID.getValue()))
                .officeId(serverRequest.queryParam(OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")))
                .instituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .samityId(serverRequest.queryParam(SAMITY_ID.getValue()).orElse(""))
                .status(serverRequest.queryParam(STATUS.getValue()).orElse(""))
                .limit(Integer.parseInt(serverRequest.queryParam(LIMIT.getValue()).orElse("10")))
                .offset(Integer.parseInt(serverRequest.queryParam(OFFSET.getValue()).orElse("0")))
//                .startDate(LocalDate.parse(serverRequest.queryParam(START_DATE.getValue()).orElse(LocalDate.now().toString()), dtf).atStartOfDay())
//                .endDate(LocalDate.parse(serverRequest.queryParam(END_DATE.getValue()).orElse(LocalDate.now().toString()), dtf).atTime(LocalTime.MAX))
                .build();
    }

    private SettleRebateRequestDto buildSettleRebateRequest(ServerRequest serverRequest, SettleRebateRequestDto settleRebateRequestDto) {
        settleRebateRequestDto.setLoginId(serverRequest.headers().firstHeader(HeaderNames.LOGIN_ID.getValue()));
        settleRebateRequestDto.setMfiId(serverRequest.headers().firstHeader(HeaderNames.MFI_ID.getValue()));
        settleRebateRequestDto.setInstituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
        settleRebateRequestDto.setOfficeId(serverRequest.queryParam(OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")));
        return settleRebateRequestDto;
    }

    private GetDetailsByMemberRequestDto buildGetDetailsByMemberRequestDto(ServerRequest serverRequest) {
        return GetDetailsByMemberRequestDto.builder()
                .mfiId(serverRequest.headers().firstHeader(HeaderNames.MFI_ID.getValue()))
                .loginId(serverRequest.headers().firstHeader(HeaderNames.LOGIN_ID.getValue()))
                .memberId(serverRequest.queryParam(MEMBER_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Member Id cannot be empty")))
                .instituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .build();
    }
}
