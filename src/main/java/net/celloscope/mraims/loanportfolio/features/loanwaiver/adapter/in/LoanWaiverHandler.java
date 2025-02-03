package net.celloscope.mraims.loanportfolio.features.loanwaiver.adapter.in;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.LoanWaiverUseCase;
import net.celloscope.mraims.loanportfolio.features.loanwaiver.application.port.in.dto.request.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.Constants.DATE_FORMAT_yyyy_MM_dd;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanWaiverHandler {

    private final LoanWaiverUseCase loanWaiverUseCase;

    public Mono<ServerResponse> getLoanWaiverList(ServerRequest serverRequest) {
        return populateRequestDTOFromHeaders(buildLoanWaiverGridViewRequestDTO(serverRequest), serverRequest)
                .flatMap(loanWaiverUseCase::getLoanWaiverList)
                .flatMap(responseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDTO))
                .doOnRequest(req -> log.info("Request Received for loan waiver grid view list : {}", req))
                .doOnSuccess(res -> log.info("Successfully retrieve loan waiver grid view list : {}", res))
                .doOnError(err -> log.info("Error occurred while getting loan waiver grid view list : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getLoanWaiverInformationById(ServerRequest serverRequest) {
        return populateRequestDTOFromHeaders(LoanWaiverDetailViewRequestDTO.builder()
                .id(serverRequest.queryParam(QueryParams.ID.getValue()).orElse("")).build(), serverRequest)
                .flatMap(loanWaiverUseCase::getLoanWaiverDetailView)
                .flatMap(responseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDTO))
                .doOnRequest(req -> log.info("Request Received for loan waiver detail view : {}", req))
                .doOnSuccess(res -> log.info("Successfully retrieve loan waiver detail view : {}", res))
                .doOnError(err -> log.info("Error occurred while getting loan waiver detail view : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> getMemberInformationForLoanWaiver(ServerRequest serverRequest) {
        return populateRequestDTOFromHeaders(LoanWaiverMemberDetailViewRequestDTO.builder()
                .memberId(serverRequest.queryParam(QueryParams.MEMBER_ID.getValue()).orElse("")).build(), serverRequest)
                .flatMap(loanWaiverUseCase::getLoanWaiverMemberDetailView)
                .flatMap(responseDTO -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(responseDTO))
                .doOnRequest(req -> log.info("Request Received for loan waiver member detail view : {}", req))
                .doOnSuccess(res -> log.info("Successfully retrieve loan waiver member detail view : {}", res))
                .doOnError(err -> log.info("Error occurred while getting loan waiver member detail view : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> createLoanWaiverByLoanAccountId(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanWaiverCreateUpdateRequestDTO.class)
                .flatMap(requestDTO -> populateRequestDTOFromHeaders(requestDTO, serverRequest))
                .flatMap(loanWaiverUseCase::createLoanWaiver)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnRequest(req -> log.info("Request Received for create loan waiver : {}", req))
                .doOnSuccess(res -> log.info("Successfully create loan waiver : {}", res))
                .doOnError(err -> log.info("Error occurred while creating loan waiver : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class,  e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> updateLoanWaiverByLoanAccountId(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanWaiverCreateUpdateRequestDTO.class)
                .flatMap(requestDTO -> populateRequestDTOFromHeaders(requestDTO, serverRequest))
                .flatMap(loanWaiverUseCase::updateLoanWaiver)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnRequest(req -> log.info("Request Received for update loan waiver : {}", req))
                .doOnSuccess(res -> log.info("Successfully update loan waiver : {}", res))
                .doOnError(err -> log.info("Error occurred while updating loan waiver : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class,  e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> submitLoanWaiverByLoanAccountId(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoanWaiverSubmitRequestDTO.class)
                .flatMap(requestDTO -> populateRequestDTOFromHeaders(requestDTO, serverRequest))
                .flatMap(loanWaiverUseCase::submitLoanWaiver)
                .flatMap(response -> ServerResponse.ok().bodyValue(response))
                .doOnRequest(req -> log.info("Request Received for submitting loan waiver : {}", req))
                .doOnSuccess(res -> log.info("Successfully submit loan waiver : {}", res))
                .doOnError(err -> log.info("Error occurred while submitting loan waiver : {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class,  e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    private LoanWaiverGridViewRequestDTO buildLoanWaiverGridViewRequestDTO(ServerRequest request) {

        String fromDateString = request.queryParam(QueryParams.FROM_DATE.getValue()).orElse(null);
        LocalDateTime fromDate = null;
        if (fromDateString != null) {
            fromDate = LocalDate.parse(fromDateString.trim(), DateTimeFormatter.ofPattern(DATE_FORMAT_yyyy_MM_dd))
                    .atStartOfDay();
        }

        String toDateString = request.queryParam(QueryParams.TO_DATE.getValue()).orElse(null);
        LocalDateTime toDate = null;
        if (toDateString != null) {
            toDate = LocalDate.parse(toDateString.trim(), DateTimeFormatter.ofPattern(DATE_FORMAT_yyyy_MM_dd))
                    .atTime(23, 59, 59);
        }

        return LoanWaiverGridViewRequestDTO.builder()
                .samityId(request.queryParam(QueryParams.SAMITY_ID.getValue()).orElse(""))
                .status(request.queryParam(QueryParams.STATUS.getValue()).orElse(""))
                .limit(Integer.valueOf(request.queryParam(QueryParams.LIMIT.getValue()).orElse("10")))
                .offset(Integer.valueOf(request.queryParam(QueryParams.OFFSET.getValue()).orElse("0")))
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
    }

    private <T extends GenericLoanWaiverRequestDTO> Mono<T> populateRequestDTOFromHeaders(T requestDTO, ServerRequest serverRequest) {
        return Mono.just(requestDTO)
                .map(dto -> {
                    dto.setMfiId(serverRequest.headers().firstHeader(QueryParams.MFI_ID.getValue()));
                    dto.setLoginId(serverRequest.headers().firstHeader(QueryParams.LOGIN_ID.getValue()));
                    dto.setUserRole(serverRequest.headers().firstHeader(QueryParams.USER_ROLE.getValue()));
                    dto.setInstituteOid(serverRequest.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElse(""));
                    dto.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    return dto;
                });
    }
}
