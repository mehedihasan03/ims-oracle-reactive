package net.celloscope.mraims.loanportfolio.features.savingsaccount.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.filters.HeaderNames;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.ISavingsAccountUseCase;
import net.celloscope.mraims.loanportfolio.features.savingsaccount.application.port.in.dto.SavingsAccountActivationRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.INSTITUTE_OID;
import static net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams.OFFICE_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
@RequiredArgsConstructor
@Slf4j
public class SavingsAccountHandler {
    private final ISavingsAccountUseCase savingsAccountUseCase;

    public Mono<ServerResponse> activateSavingsAccount(ServerRequest request) {
        return request.bodyToMono(SavingsAccountActivationRequestDto.class)
                        .flatMap(reqBody -> savingsAccountUseCase.activateSavingsAccountWIthOpeningBalance(buildSavingsAccountActivationRequestDto(request, reqBody)))
                .flatMap(savingsAccountResponseDTO ->
                        ServerResponse
                                .ok()
                                .contentType(APPLICATION_JSON)
                                .bodyValue(savingsAccountResponseDTO))
                .doOnRequest(r -> log.info("Request received for activating savings account with opening balance: {}", r))
                .doOnSuccess(res -> log.info("Response for activating savings account with opening balance: {}", res.statusCode()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }

    private SavingsAccountActivationRequestDto buildSavingsAccountActivationRequestDto(ServerRequest serverRequest, SavingsAccountActivationRequestDto requestDto) {
        requestDto.setLoginId(serverRequest.headers().firstHeader(HeaderNames.LOGIN_ID.getValue()));
        requestDto.setMfiId(serverRequest.headers().firstHeader(HeaderNames.MFI_ID.getValue()));
        requestDto.setInstituteOid(serverRequest.queryParam(INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
        requestDto.setOfficeId(serverRequest.queryParam(OFFICE_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Office Id cannot be empty")));
        return requestDto;
    }
}
