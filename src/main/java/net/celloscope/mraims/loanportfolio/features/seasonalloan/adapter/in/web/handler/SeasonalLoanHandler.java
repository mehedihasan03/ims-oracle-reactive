package net.celloscope.mraims.loanportfolio.features.seasonalloan.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.rebate.application.port.in.dto.SettleRebateRequestDto;
import net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto.SeasonalLoanCollectionRequestDto;
import net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.dto.SeasonalLoanGridRequestDto;
import net.celloscope.mraims.loanportfolio.features.seasonalloan.application.port.in.SeasonalLoanUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import org.testng.util.Strings;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.*;
import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.END_DATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SeasonalLoanHandler {

    private final SeasonalLoanUseCase useCase;

    public Mono<ServerResponse> getSeasonalLoanDetailView(ServerRequest serverRequest) {
        String id = serverRequest.queryParam(QueryParams.ID.getValue()).orElse("");
        String officeId = serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("");

        return this.validateRequestForDetailView(id, officeId)
                .flatMap(aBoolean -> useCase.getSeasonalLoanDetailView(id, officeId))
                .flatMap(response -> ServerResponse.ok()
                        .bodyValue(response))
                .onErrorResume(ExceptionHandlerUtil.class,
                        e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance),
                        e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getSeasonalLoanGridView(ServerRequest serverRequest){
        SeasonalLoanGridRequestDto requestDto = buildSeasonalLoanGridRequestDto(serverRequest);
        return useCase.getSeasonalLoanGridView(requestDto)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received for Seasonal Loan Grid View By Office: {}", requestDto))
                .doOnSuccess(res -> log.info("Response for Seasonal Loan Grid View By Office: {}", res.statusCode()))
                .doOnError(err -> log.error("Error occurred while processing Seasonal Loan Grid View By Office Request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> collectSeasonalLoan(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(SeasonalLoanCollectionRequestDto.class)
                .map(requestDto -> buildSeasonalLoanCollectionRequest(serverRequest, requestDto))
                .flatMap(useCase::collectSeasonalLoan)
                .flatMap(settleRebateResponseDto -> ServerResponse.ok().bodyValue(settleRebateResponseDto))
                .doOnRequest(r -> log.info("Request received for settling Rebate: {}", r))
                .doOnSuccess(res -> log.info("Response for settling Rebate: {}", res.statusCode()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private SeasonalLoanCollectionRequestDto buildSeasonalLoanCollectionRequest(ServerRequest serverRequest, SeasonalLoanCollectionRequestDto requestDto) {
        requestDto.setLoginId(serverRequest.queryParam(LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login Id cannot be empty")));
        requestDto.setMfiId(serverRequest.queryParam(MFI_ID.getValue()).orElseThrow(() -> new ServerWebInputException("MFI Id cannot be empty")));
        requestDto.setInstituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
        requestDto.setOfficeId(serverRequest.queryParam(OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")));
        return requestDto;
    }

    private SeasonalLoanGridRequestDto buildSeasonalLoanGridRequestDto(ServerRequest serverRequest) {
        return SeasonalLoanGridRequestDto.builder()
                .officeId(serverRequest.queryParam(OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")))
                .instituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")))
                .samityId(serverRequest.queryParam(SAMITY_ID.getValue()).orElse(""))
                .memberId(serverRequest.queryParam(MEMBER_ID.getValue()).orElse(""))
                .fieldOfficerId(serverRequest.queryParam(FIELD_OFFICER_ID.getValue()).orElse(""))
                .loanAccountId(serverRequest.queryParam(LOAN_ACCOUNT_ID.getValue()).orElse(""))
                .limit(Integer.parseInt(serverRequest.queryParam(LIMIT.getValue()).orElse("10")))
                .offset(Integer.parseInt(serverRequest.queryParam(OFFSET.getValue()).orElse("0")))
                .build();
    }

    private Mono<Boolean> validateRequestForDetailView(String id, String officeId) {
        return Mono.just(Tuples.of(id, officeId))
                .filter(tuple -> !Strings.isNullOrEmpty(tuple.getT1()) && !Strings.isNullOrEmpty(tuple.getT2()))
                .switchIfEmpty(Mono.error(new ExceptionHandlerUtil(HttpStatus.BAD_REQUEST, "id and officeId are required for detail view.")))
                .map(tuple -> true);
    }
}
