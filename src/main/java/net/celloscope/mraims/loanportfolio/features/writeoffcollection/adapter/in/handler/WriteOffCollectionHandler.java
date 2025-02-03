package net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.LoanWriteOffGridViewByOfficeRequestDto;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.WriteOffCollectionAccountDataRequestDto;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.adapter.in.dto.WriteOffCollectionAccountDataResponseDto;
import net.celloscope.mraims.loanportfolio.features.writeoffcollection.application.port.in.WriteOffCollectionUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.END_DATE;
import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.START_DATE;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.END_DATE;
import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.START_DATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class WriteOffCollectionHandler {

    private final WriteOffCollectionUseCase useCase;

    public Mono<ServerResponse> getWriteOffCollectionData(ServerRequest serverRequest) {
        log.info("Request Landed on getWriteOffCollectionData");
        return useCase.getWriteOffCollectionAccountDataV2(this.buildWriteOffCollectionAccountDataRequest(serverRequest))
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for write off collection eligible account data : {}", req))
                .doOnSuccess(res -> log.info("Successfully retrieve write off collection eligible account data : {}", res))
                .doOnError(err -> log.info("Error occurred while getting write off collection eligible account data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private WriteOffCollectionAccountDataRequestDto buildWriteOffCollectionAccountDataRequest(ServerRequest request) {
        return WriteOffCollectionAccountDataRequestDto.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")))
                .loanAccountId(request.queryParam(QueryParams.LOAN_ACCOUNT_ID.getValue()).orElse(""))
                .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                .status(request.queryParam(QueryParams.STATUS.getValue()).orElse(""))
                .searchText(request.queryParam(QueryParams.SEARCH_TEXT.getValue()).orElse(""))
                .build();
    }

    public Mono<ServerResponse> createWriteOffCollection(ServerRequest serverRequest) {
        log.info("Request Landed on createWriteOffCollection");
        return buildRequestBody(serverRequest)
                .flatMap(useCase::createWriteOffCollection)
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for create write off collection data : {}", req))
                .doOnSuccess(res -> log.info("Successfully created write off collection data : {}", res))
                .doOnError(err -> log.info("Error occurred while creating write off collection data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> updateWriteOffCollection(ServerRequest request) {
        log.info("Request Landed on updateWriteOffCollection");
        return buildRequestBody(request)
                .flatMap(useCase::updateWriteOffCollection)
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for update write off collection data : {}", req))
                .doOnSuccess(res -> log.info("Successfully update write off collection data : {}", res))
                .doOnError(err -> log.info("Error occurred while updating write off collection data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    public Mono<ServerResponse> submitLoanWriteOffCollection(ServerRequest request) {
        log.info("Request Landed on submitLoanWriteOffCollection");
        return buildRequestBody(request)
                .flatMap(useCase::submitLoanWriteOffCollectionData)
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for submit write off collection data : {}", req))
                .doOnSuccess(res -> log.info("Successfully authorize submit off collection data : {}", res))
                .doOnError(err -> log.info("Error occurred while submit write off collection data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private static Mono<WriteOffCollectionAccountDataRequestDto> buildRequestBody(ServerRequest request) {
        return request.bodyToMono(WriteOffCollectionAccountDataRequestDto.class)
                .map(requestDto -> {
                    requestDto.setMfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()));
                    requestDto.setOfficeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")));
                    requestDto.setLoginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()));
                    requestDto.setUserRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()));
                    requestDto.setInstituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
                    return requestDto;
                });
    }


    public Mono<ServerResponse> getCollectedWriteOffLoanData(ServerRequest serverRequest) {
        log.info("Request Landed on getCollectedWriteOffLoanData");
        return useCase.getWriteOffEligibleAccountList(buildCollectedWriteOffLoanData(serverRequest))
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for collected write off data : {}", req))
                .doOnSuccess(res -> log.info("Successfully retrieve collected write off data : {}", res))
                .doOnError(err -> log.info("Error occurred while getting collected write off data by office id : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private LoanWriteOffGridViewByOfficeRequestDto buildCollectedWriteOffLoanData(ServerRequest request) {
        final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LoanWriteOffGridViewByOfficeRequestDto.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .officeId(request.queryParam(QueryParams.OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")))
                .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(""))
                .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                .startDate(LocalDate.parse(request.queryParam(START_DATE.getValue()).orElse(LocalDate.now().toString()), dtf).atStartOfDay())
                .endDate(LocalDate.parse(request.queryParam(END_DATE.getValue()).orElse(LocalDate.now().toString()), dtf).atTime(LocalTime.MAX))
                .build();
    }

    public Mono<ServerResponse> getDetailWriteOffCollection(ServerRequest request) {
        return useCase.getDetailsCollectedWriteOffData(buildDetailWriteOffCollectionAccountData(request))
                .flatMap(responseDto -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDto))
                .doOnRequest(req -> log.info("Request Received for details write off collection  account data : {}", req))
                .doOnSuccess(res -> log.info("Successfully retrieve details write off collection eligible account data : {}", res))
                .doOnError(err -> log.info("Error occurred while getting details write off collection  account data : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private WriteOffCollectionAccountDataRequestDto buildDetailWriteOffCollectionAccountData(ServerRequest request) {
        return WriteOffCollectionAccountDataRequestDto.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .userRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .id(request.queryParam(QueryParams.ID.getValue()).orElse(""))
                .build();
    }

    public Mono<ServerResponse> deleteLoanWriteOffData(ServerRequest request) {
        return useCase.deleteWriteOffData(buildDeleteLoanWriteOffDataRequest(request))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(req -> log.info("Request Received to delete write off data: {}", req))
                .doOnSuccess(res -> log.info("Successfully deleted write off data: {}", res))
                .doOnError(err -> log.info("Error occurred while processing delete write off data request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private WriteOffCollectionAccountDataRequestDto buildDeleteLoanWriteOffDataRequest(ServerRequest request) {
        return WriteOffCollectionAccountDataRequestDto.builder()
                .mfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()))
                .loginId(request.headers().firstHeader(QueryParams.LOGIN_ID.getValue()))
                .instituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .id(request.queryParam(QueryParams.ID.getValue()).orElseThrow(() -> new ServerWebInputException("Id cannot be empty")))
                .build();
    }

}
