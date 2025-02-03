package net.celloscope.mraims.loanportfolio.features.transactionadjustment.application.port.in;

import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.dto.request.TransactionAdjustmentRequestDto;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.dto.response.TransactionAdjustmentResponseDto;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionAdjustmentUseCase {
    Mono<TransactionAdjustmentResponseDto> adjustTransaction(TransactionAdjustmentRequestDto requestDto);

    Flux<TransactionAdjustmentResponseDto> getTransactionAdjustmentsByManagementProcessIdAndTransactionCode(String managementProcessId, String transactionCode);

    Flux<TransactionAdjustmentResponseDto> getTransactionAdjustmentsByManagementProcessIdAndTransactionCodeAndSavingsType(String managementProcessId, String transactionCode, String savingsType);

    Flux<TransactionAdjustmentResponseDto> getTransactionAdjustmentsByManagementProcessIdAndTransactionCodeAndPaymentMode(String managementProcessId, String transactionCode, String paymentMode);
}
