package net.celloscope.mraims.loanportfolio.features.equalInstallment.adapter.in.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.equalInstallment.application.port.in.EqualInstallmentUseCase;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class EqualInstallmentHandler {
    private final EqualInstallmentUseCase equalInstallmentUseCase;

    public Mono<ServerResponse> getEqualInstallmentAmount(ServerRequest serverRequest) {
        BigDecimal loanAmount = new BigDecimal(serverRequest.queryParam("loanAmount").get());
        BigDecimal annualServiceChargeRate = new BigDecimal(serverRequest.queryParam("annualServiceChargeRate").get());
        Integer noOfInstallments = Integer.parseInt(serverRequest.queryParam("noOfInstallments").get());
        Integer loanTerm = Integer.parseInt(serverRequest.queryParam("loanTerm").orElse("12"));

        return equalInstallmentUseCase
                .getEqualInstallmentAmountWhenPaymentPeriodUndefined(loanAmount.doubleValue(), annualServiceChargeRate.doubleValue(), noOfInstallments, loanTerm)
                .flatMap(ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)::bodyValue);
    }

    public Mono<ServerResponse> getEqualInstallmentAmountAccordingToInterestCalcMethod(ServerRequest serverRequest) {
        BigDecimal loanAmount = new BigDecimal(serverRequest.queryParam("loanAmount").orElseThrow(() -> new IllegalArgumentException("loanAmount is required")));
        BigDecimal annualServiceChargeRate = new BigDecimal(serverRequest.queryParam("annualServiceChargeRate").orElseThrow(() -> new IllegalArgumentException("annualServiceChargeRate is required")));
        Integer noOfInstallments = Integer.parseInt(serverRequest.queryParam("noOfInstallments").orElseThrow(() -> new IllegalArgumentException("noOfInstallments is required")));
        Integer loanTerm = Integer.parseInt(serverRequest.queryParam("loanTerm").orElse("12"));
        String interestCalcMethod = serverRequest.queryParam("interestCalcMethod").orElseThrow(() -> new IllegalArgumentException("interestCalcMethod is required"));


        return equalInstallmentUseCase
                .getEqualInstallmentAmountAccordingToInterestCalcMethod(loanAmount, annualServiceChargeRate, noOfInstallments, loanTerm, interestCalcMethod)
                .flatMap(ServerResponse
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)::bodyValue);
    }
}
