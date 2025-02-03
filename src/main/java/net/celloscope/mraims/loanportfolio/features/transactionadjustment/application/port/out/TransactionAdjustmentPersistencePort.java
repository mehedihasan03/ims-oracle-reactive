package net.celloscope.mraims.loanportfolio.features.transactionadjustment.application.port.out;

import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.TransactionAdjustment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionAdjustmentPersistencePort {
    Mono<TransactionAdjustment> saveTransactionAdjustment(TransactionAdjustment transactionAdjustment);

    Flux<TransactionAdjustment> getTransactionAdjustmentByManagementProcessIdAndTransactionCode(String managementProcessId, String transactionCode);

    Flux<TransactionAdjustment> getTransactionAdjustmentByManagementProcessIdAndTransactionCodeAndSavingsType(String managementProcessId, String transactionCode, String savingsTypeId);

    Flux<TransactionAdjustment> getTransactionAdjustmentByManagementProcessIdAndTransactionCodeAndPaymentMode(String managementProcessId, String transactionCode, String paymentMode);
}
