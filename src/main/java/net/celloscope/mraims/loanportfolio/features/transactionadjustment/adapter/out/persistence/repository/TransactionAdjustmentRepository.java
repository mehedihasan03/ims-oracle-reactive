package net.celloscope.mraims.loanportfolio.features.transactionadjustment.adapter.out.persistence.repository;

import net.celloscope.mraims.loanportfolio.features.transactionadjustment.adapter.out.persistence.entity.TransactionAdjustmentEntity;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.TransactionAdjustment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface TransactionAdjustmentRepository extends ReactiveCrudRepository<TransactionAdjustmentEntity, String> {
    Flux<TransactionAdjustmentEntity> findAllByManagementProcessIdAndTransactionCode(String managementProcessId, String transactionCode);

    Flux<TransactionAdjustmentEntity> findAllByManagementProcessIdAndTransactionCodeAndSavingsTypeIdAndSavingsTypeIdNotNull(String managementProcessId, String transactionCode, String savingsTypeId);

    Flux<TransactionAdjustmentEntity> findAllByManagementProcessIdAndTransactionCodeAndPaymentMode(String managementProcessId, String transactionCode, String paymentMode);
}
