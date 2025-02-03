package net.celloscope.mraims.loanportfolio.features.transactionadjustment.adapter.out.persistence;

import lombok.extern.slf4j.Slf4j;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.adapter.out.persistence.entity.TransactionAdjustmentEntity;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.adapter.out.persistence.repository.TransactionAdjustmentRepository;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.application.port.out.TransactionAdjustmentPersistencePort;
import net.celloscope.mraims.loanportfolio.features.transactionadjustment.domain.TransactionAdjustment;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TransactionAdjustmentPersistenceAdapter implements TransactionAdjustmentPersistencePort {
    private final TransactionAdjustmentRepository repository;
    private final ModelMapper modelMapper;

    public TransactionAdjustmentPersistenceAdapter(TransactionAdjustmentRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Mono<TransactionAdjustment> saveTransactionAdjustment(TransactionAdjustment transactionAdjustment) {
        return repository.save(modelMapper.map(transactionAdjustment, TransactionAdjustmentEntity.class))
                .doOnNext(transactionAdjustmentEntity -> log.info("Transaction adjustment saved: {}", transactionAdjustmentEntity))
                .map(transactionAdjustmentEntity -> modelMapper.map(transactionAdjustmentEntity, TransactionAdjustment.class));
    }

    @Override
    public Flux<TransactionAdjustment> getTransactionAdjustmentByManagementProcessIdAndTransactionCode(String managementProcessId, String transactionCode) {
        return repository.findAllByManagementProcessIdAndTransactionCode(managementProcessId, transactionCode)
                .doOnNext(transactionAdjustmentEntity -> log.info("Transaction adjustment retrieved: {}", transactionAdjustmentEntity))
                .map(transactionAdjustmentEntity -> modelMapper.map(transactionAdjustmentEntity, TransactionAdjustment.class));
    }

    @Override
    public Flux<TransactionAdjustment> getTransactionAdjustmentByManagementProcessIdAndTransactionCodeAndSavingsType(String managementProcessId, String transactionCode, String savingsTypeId) {
        return repository.findAllByManagementProcessIdAndTransactionCodeAndSavingsTypeIdAndSavingsTypeIdNotNull(managementProcessId, transactionCode, savingsTypeId)
                .map(transactionAdjustmentEntity -> modelMapper.map(transactionAdjustmentEntity, TransactionAdjustment.class));
    }

    @Override
    public Flux<TransactionAdjustment> getTransactionAdjustmentByManagementProcessIdAndTransactionCodeAndPaymentMode(String managementProcessId, String transactionCode, String paymentMode) {
        return repository.findAllByManagementProcessIdAndTransactionCodeAndPaymentMode(managementProcessId, transactionCode, paymentMode)
                .map(transactionAdjustmentEntity -> modelMapper.map(transactionAdjustmentEntity, TransactionAdjustment.class));
    }
}
