package net.celloscope.mraims.loanportfolio.features.transactionadjustment.adapter.in.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.application.port.in.TransactionAdjustmentUseCase;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.dto.request.TransactionAdjustmentRequestDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@Component
@Slf4j
public class TransactionAdjustmentHandler {
    private final TransactionAdjustmentUseCase transactionAdjustmentUseCase;

    public TransactionAdjustmentHandler(TransactionAdjustmentUseCase transactionAdjustmentUseCase) {
        this.transactionAdjustmentUseCase = transactionAdjustmentUseCase;
    }


    public Mono<ServerResponse> adjustTransaction(ServerRequest serverRequest) {
        String loginId = serverRequest.queryParam(QueryParams.LOGIN_ID.getValue()).orElseThrow(() -> new IllegalArgumentException("Login ID is required"));
        String mfiId = serverRequest.queryParam(QueryParams.MFI_ID.getValue()).orElseThrow(() -> new IllegalArgumentException("MFI ID is required"));
        return serverRequest.bodyToMono(TransactionAdjustmentRequestDto.class)
                .map(requestDto -> {
                    requestDto.setLoginId(loginId);
                    requestDto.setMfiId(mfiId);
                    return requestDto;
                })
                .flatMap(transactionAdjustmentUseCase::adjustTransaction)
                .flatMap(accruedInterestResponseDTOS -> ServerResponse
                        .created(serverRequest.uri())
                        .bodyValue(accruedInterestResponseDTOS))
                .doOnError(err -> log.info("Error occurred while adjusting transaction: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }
}
