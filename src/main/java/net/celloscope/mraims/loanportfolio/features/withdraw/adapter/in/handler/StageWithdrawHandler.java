package net.celloscope.mraims.loanportfolio.features.withdraw.adapter.in.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.core.util.helper.HelperUtil;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.StageWithdrawUseCase;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands.StageWithdrawCommand;
import net.celloscope.mraims.loanportfolio.features.withdraw.application.port.in.commands.WithdrawRequestDto;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
public class StageWithdrawHandler {
    private final StageWithdrawUseCase stageWithdrawUseCase;

    public StageWithdrawHandler(StageWithdrawUseCase stageWithdrawUseCase) {
        this.stageWithdrawUseCase = stageWithdrawUseCase;
    }

    public Mono<ServerResponse> stageWithdraw(ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(StageWithdrawCommand.class)
                .map(command -> {
                    if(HelperUtil.checkIfNullOrEmpty(command.getOfficeId())){
                        command.setOfficeId(serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse(""));
                    }
                    command.setLoginId(serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElse(""));
                    return command;
                })
                .flatMap(stageWithdrawUseCase::stageWithdraw)
                .flatMap(response -> ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received for withdraw: {}", r))
                .doOnSuccess(res -> log.info("Response for withdraw: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing withdraw request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }

    public Mono<ServerResponse> updateWithdrawData(ServerRequest request) {
        return request
                .bodyToMono(WithdrawRequestDto.class)
                .map(withdrawRequestDto -> {
                    withdrawRequestDto.setMfiId(request.headers().firstHeader(QueryParams.MFI_ID.getValue()));
                    withdrawRequestDto.setUserRole(request.headers().firstHeader(QueryParams.USER_ROLE.getValue()));
                    withdrawRequestDto.setInstituteOid(request.queryParam(QueryParams.INSTITUTE_OID.getValue()).orElseThrow(() -> new ServerWebInputException("Institute Oid cannot be empty")));
                    withdrawRequestDto.setLoginId(request.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new ServerWebInputException("Login id cannot be empty")));
                    return withdrawRequestDto;
                })
                .flatMap(stageWithdrawUseCase::updateWithdrawalAmount)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .doOnRequest(r -> log.info("Request Received for edit withdraw amount: {}", r))
                .doOnSuccess(res -> log.info("Response for edit withdraw amount: {}", res.statusCode()))
                .doOnError(err -> log.info("Error occurred while processing edit withdraw request: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, request))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, request));
    }
}
