package net.celloscope.mraims.loanportfolio.features.savingsinterest.adapter.in.web.handler;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.core.util.enums.QueryParams;
import net.celloscope.mraims.loanportfolio.core.util.exception.ErrorHandler;
import net.celloscope.mraims.loanportfolio.core.util.exception.ExceptionHandlerUtil;
import net.celloscope.mraims.loanportfolio.features.savingsinterest.application.port.in.AccruedInterestUseCase;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Component
@Slf4j
public class AccrueInterestHandler {
    private final AccruedInterestUseCase accruedInterestUseCase;

    public AccrueInterestHandler(AccruedInterestUseCase accruedInterestUseCase) {
        this.accruedInterestUseCase = accruedInterestUseCase;
    }


    public Mono<ServerResponse> getAccruedInterestEntities(ServerRequest serverRequest) {
        String managementProcessId = serverRequest.queryParam(QueryParams.MANAGEMENT_PROCESS_ID.getValue()).orElse("");
        String officeId = serverRequest.queryParam(QueryParams.OFFICE_ID.getValue()).orElse("");

        return accruedInterestUseCase
                .getAccruedInterestEntriesByManagementProcessIdAndOfficeId(managementProcessId, officeId)
                .flatMap(accruedInterestResponseDTOS -> ServerResponse
                        .ok()
                        .bodyValue(accruedInterestResponseDTOS))
                .doOnError(err -> log.info("Error occurred while getting accrued interest entries: {}", err.getMessage()))
                .onErrorResume(ExceptionHandlerUtil.class, e -> ErrorHandler.buildErrorResponseForBusiness(e, serverRequest))
                .onErrorResume(Predicate.not(ExceptionHandlerUtil.class::isInstance), e -> ErrorHandler.buildErrorResponseForUncaught(e, serverRequest));
    }
}
